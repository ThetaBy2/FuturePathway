package com.futurepathway.service.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurepathway.dto.MatchResultDto;
import com.futurepathway.entity.Certificate;
import com.futurepathway.entity.Job;
import com.futurepathway.entity.Project;
import com.futurepathway.entity.Roadmap;
import com.futurepathway.entity.Skill;
import com.futurepathway.entity.SkillAnalysis;
import com.futurepathway.entity.Student;
import com.futurepathway.entity.StudentSkill;
import com.futurepathway.exception.AiServiceException;
import com.futurepathway.exception.ResourceNotFoundException;
import com.futurepathway.repository.CertificateRepository;
import com.futurepathway.repository.JobRepository;
import com.futurepathway.repository.JobSkillRepository;
import com.futurepathway.repository.ProjectRepository;
import com.futurepathway.repository.RoadmapRepository;
import com.futurepathway.repository.SkillAnalysisRepository;
import com.futurepathway.repository.StudentRepository;
import com.futurepathway.repository.StudentSkillRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spec §5 — the four Gemini integration points.
 *
 * <ol>
 *   <li>{@link #resumeSkillExtraction(Long)}</li>
 *   <li>{@link #generateRoadmap(Long, String)}</li>
 *   <li>{@link #computeProfileMatch(Long, Long)}</li>
 *   <li>{@link #trendingSkillsInsight()}</li>
 * </ol>
 * When Gemini is unreachable / unconfigured, a deterministic heuristic fallback
 * answers instead; every response carries {@code source = "gemini" | "heuristic-fallback"}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    public static final String SOURCE_GEMINI = "gemini";
    public static final String SOURCE_FALLBACK = "heuristic-fallback";

    private final GeminiClient geminiClient;
    private final GeminiPromptService prompts;
    private final ObjectMapper objectMapper;

    private final StudentRepository studentRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final ProjectRepository projectRepository;
    private final CertificateRepository certificateRepository;
    private final JobRepository jobRepository;
    private final JobSkillRepository jobSkillRepository;
    private final SkillAnalysisRepository skillAnalysisRepository;
    private final RoadmapRepository roadmapRepository;

    // ── 1. resumeSkillExtraction ─────────────────────────────────────────

    @Transactional
    public AnalysisResult resumeSkillExtraction(Long studentId) {
        Student student = requireStudent(studentId);
        String profileText = buildProfileText(student);
        try {
            String json = geminiClient.generateJson(prompts.resumeSkillExtractionPrompt(profileText));
            JsonNode root = objectMapper.readTree(json);
            SkillAnalysis analysis = SkillAnalysis.builder()
                    .student(student)
                    .strongSkills(toListOfMaps(root.path("strong_skills")))
                    .weakSkills(toListOfMaps(root.path("weak_skills")))
                    .summaryText(root.path("summary").asText(""))
                    .source(SOURCE_GEMINI)
                    .build();
            SkillAnalysis saved = skillAnalysisRepository.save(analysis);
            log.info("Gemini skill analysis saved for student {} (analysis {})", studentId, saved.getId());
            return new AnalysisResult(saved, SOURCE_GEMINI);
        } catch (AiServiceException e) {
            log.warn("Gemini unavailable for student {}, using heuristic fallback: {}", studentId, e.getMessage());
            SkillAnalysis fallback = skillAnalysisRepository.save(heuristicAnalysis(student));
            return new AnalysisResult(fallback, SOURCE_FALLBACK);
        } catch (Exception e) {
            throw new AiServiceException("Could not parse Gemini skill analysis: " + e.getMessage(), e);
        }
    }

    // ── 2. generateRoadmap ──────────────────────────────────────────────

    @Transactional
    public RoadmapResult generateRoadmap(Long studentId, String targetRole) {
        Student student = requireStudent(studentId);
        List<String> weakSkills = skillAnalysisRepository
                .findTopByStudentIdOrderByGeneratedAtDesc(studentId)
                .map(a -> a.getWeakSkills().stream()
                        .map(m -> String.valueOf(m.getOrDefault("skill", "")))
                        .filter(s -> !s.isBlank())
                        .toList())
                .orElse(List.of());
        String profileText = buildProfileText(student);
        try {
            String json = geminiClient.generateJson(prompts.roadmapPrompt(profileText, targetRole, weakSkills));
            JsonNode root = objectMapper.readTree(json);
            Roadmap roadmap = Roadmap.builder()
                    .student(student)
                    .targetRole(root.path("target_role").asText(targetRole))
                    .milestones(toListOfMaps(root.path("milestones")))
                    .source(SOURCE_GEMINI)
                    .build();
            Roadmap saved = roadmapRepository.save(roadmap);
            return new RoadmapResult(saved, SOURCE_GEMINI);
        } catch (AiServiceException e) {
            log.warn("Gemini unavailable for roadmap (student {}), using heuristic fallback", studentId);
            Roadmap fallback = roadmapRepository.save(heuristicRoadmap(student, targetRole, weakSkills));
            return new RoadmapResult(fallback, SOURCE_FALLBACK);
        } catch (Exception e) {
            throw new AiServiceException("Could not parse Gemini roadmap: " + e.getMessage(), e);
        }
    }

    // ── 3. computeProfileMatch ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public MatchResultDto computeProfileMatch(Long studentId, Long jobId) {
        Student student = requireStudent(studentId);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));
        String profileText = buildProfileText(student);
        String jobText = buildJobText(job);
        try {
            String json = geminiClient.generateJson(prompts.profileMatchPrompt(profileText, jobText));
            JsonNode root = objectMapper.readTree(json);
            BigDecimal score = clamp(root.path("match_score").asDouble(0));
            List<String> missing = new ArrayList<>();
            root.path("missing_skills").forEach(n -> missing.add(n.asText()));
            return new MatchResultDto(studentId, jobId, job.getTitle(), score,
                    root.path("reasoning").asText(""), missing, SOURCE_GEMINI);
        } catch (AiServiceException e) {
            log.warn("Gemini unavailable for match (student {}, job {}), using heuristic fallback",
                    studentId, jobId);
            return heuristicMatch(student, job);
        } catch (Exception e) {
            throw new AiServiceException("Could not parse Gemini match result: " + e.getMessage(), e);
        }
    }

    // ── 4. trendingSkillsInsight ─────────────────────────────────────────

    /** Summarises in-demand skills; used by the scheduler and the portal API. */
    public String trendingSkillsInsight() {
        List<String> lines = topDemandedSkills(10).stream()
                .map(entry -> "- " + entry.getKey().getName() + ": " + entry.getValue() + " active postings")
                .toList();
        if (lines.isEmpty()) {
            return "No active job postings yet, so there is no demand signal to summarise.";
        }
        try {
            String json = geminiClient.generateJson(prompts.trendingInsightPrompt(lines));
            return objectMapper.readTree(json).path("insight").asText("");
        } catch (AiServiceException e) {
            log.warn("Gemini unavailable for trending insight, using heuristic fallback");
            String top = topDemandedSkills(5).stream()
                    .map(entry -> entry.getKey().getName() + " (" + entry.getValue() + " postings)")
                    .collect(Collectors.joining(", "));
            return "Based on active postings on this portal, the most demanded skills right now are: "
                    + top + ". Students should prioritise these in projects and certifications.";
        } catch (Exception e) {
            throw new AiServiceException("Could not parse Gemini trending insight: " + e.getMessage(), e);
        }
    }

    // ── profile / job text builders ──────────────────────────────────────

    private String buildProfileText(Student student) {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(student.getUser().getName()).append('\n');
        sb.append("Branch: ").append(student.getBranch())
                .append(", Batch: ").append(student.getBatchYear())
                .append(", CGPA: ").append(student.getCgpa())
                .append(", Backlogs: ").append(student.getBacklogCount()).append('\n');
        List<StudentSkill> skills = studentSkillRepository.findByStudentId(student.getId());
        sb.append("Skills:\n");
        for (StudentSkill ss : skills) {
            sb.append("  - ").append(ss.getSkill().getName())
                    .append(" (level ").append(ss.getProficiencyLevel())
                    .append("/5, ").append(ss.getSource().name().toLowerCase(Locale.ROOT)).append(")\n");
        }
        sb.append("Projects:\n");
        for (Project p : projectRepository.findByStudentId(student.getId())) {
            sb.append("  - ").append(p.getTitle()).append(": ")
                    .append(nullToEmpty(p.getDescription()))
                    .append(" [stack: ").append(nullToEmpty(p.getTechStack())).append("]\n");
        }
        sb.append("Certificates:\n");
        for (Certificate c : certificateRepository.findByStudentId(student.getId())) {
            sb.append("  - ").append(c.getTitle()).append(" by ").append(nullToEmpty(c.getIssuingOrg()))
                    .append(" (").append(c.getVerificationStatus()).append(")\n");
        }
        sb.append("CV text:\n").append(nullToEmpty(student.getResumeText())).append('\n');
        return sb.toString();
    }

    private String buildJobText(Job job) {
        StringBuilder sb = new StringBuilder();
        sb.append("Title: ").append(job.getTitle()).append('\n');
        sb.append("Domain: ").append(nullToEmpty(job.getJobDomain())).append('\n');
        sb.append("Description: ").append(nullToEmpty(job.getDescription())).append('\n');
        sb.append("Required skills:\n");
        jobSkillRepository.findByJobId(job.getId()).forEach(js -> sb
                .append("  - ").append(js.getSkill().getName())
                .append(" (min level ").append(js.getRequiredProficiency()).append("/5)\n"));
        sb.append("Min CGPA: ").append(job.getMinCgpa())
                .append(", Max backlogs: ").append(job.getMaxBacklogs())
                .append(", Branches: ").append(job.getEligibleBranches()).append('\n');
        return sb.toString();
    }

    // ── heuristic fallbacks (deterministic, no network) ──────────────────

    private SkillAnalysis heuristicAnalysis(Student student) {
        List<StudentSkill> skills = studentSkillRepository.findByStudentId(student.getId());
        List<Map<String, Object>> strong = skills.stream()
                .filter(s -> s.getProficiencyLevel() >= 3)
                .sorted(Comparator.comparing(StudentSkill::getProficiencyLevel).reversed())
                .map(s -> linkedMapOf(
                        "skill", s.getSkill().getName(),
                        "level", s.getProficiencyLevel(),
                        "evidence", "Self-rated " + s.getProficiencyLevel() + "/5 (" + s.getSource() + ")"))
                .map(m -> (Map<String, Object>) m)
                .toList();
        var owned = skills.stream()
                .map(s -> s.getSkill().getName().toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        List<Map<String, Object>> weak = topDemandedSkills(10).stream()
                .filter(e -> !owned.contains(e.getKey().getName().toLowerCase(Locale.ROOT)))
                .limit(5)
                .map(e -> linkedMapOf(
                        "skill", e.getKey().getName(),
                        "gap", "In demand (" + e.getValue() + " active postings) but missing from profile",
                        "suggestion", "Build a small project using " + e.getKey().getName()
                                + " and add it to the profile"))
                .map(m -> (Map<String, Object>) m)
                .toList();
        String summary = strong.isEmpty()
                ? "Profile has no rated skills yet. Add skills, projects and CV text for a deeper analysis."
                : "Strongest areas are " + strong.stream().limit(3)
                        .map(m -> String.valueOf(m.get("skill"))).collect(Collectors.joining(", "))
                        + ". Focus next on the weak skills listed, which are in demand by recruiters.";
        return SkillAnalysis.builder()
                .student(student).strongSkills(strong).weakSkills(weak)
                .summaryText(summary).source(SOURCE_FALLBACK).build();
    }

    private Roadmap heuristicRoadmap(Student student, String targetRole, List<String> weakSkills) {
        List<String> focus = weakSkills.isEmpty() ? List.of("core " + targetRole + " fundamentals") : weakSkills;
        List<Map<String, Object>> milestones = new ArrayList<>();
        int week = 1;
        for (int i = 0; i < focus.size() && i < 4; i++) {
            String skill = focus.get(i);
            milestones.add(linkedMapOf(
                    "week", week + "-" + (week + 2),
                    "title", "Master " + skill + " for " + targetRole,
                    "tasks", List.of(
                            "Complete a structured " + skill + " course (8-10 hrs)",
                            "Build a mini-project applying " + skill,
                            "Push the project to GitHub with a README and demo"),
                    "resources", List.of(skill + " official docs", "freeCodeCamp / NPTEL " + skill + " course")));
            week += 3;
        }
        milestones.add(linkedMapOf(
                "week", week + "-" + (week + 2),
                "title", "Become job-ready as " + targetRole,
                "tasks", List.of("Polish CV with new projects", "Mock interviews (2 technical + 1 HR)",
                        "Apply to 5 matching roles on Future Pathway"),
                "resources", List.of("Future Pathway mock-interview slots", "Striver / NeetCode sheets")));
        return Roadmap.builder().student(student).targetRole(targetRole)
                .milestones(milestones.stream().map(m -> (Map<String, Object>) m).toList())
                .source(SOURCE_FALLBACK).build();
    }

    private MatchResultDto heuristicMatch(Student student, Job job) {
        var required = jobSkillRepository.findByJobId(job.getId());
        Map<String, Integer> owned = studentSkillRepository.findByStudentId(student.getId()).stream()
                .collect(Collectors.toMap(s -> s.getSkill().getName().toLowerCase(Locale.ROOT),
                        StudentSkill::getProficiencyLevel, Math::max));
        double earned = 0;
        double possible = 0;
        List<String> missing = new ArrayList<>();
        for (var js : required) {
            int need = js.getRequiredProficiency();
            possible += need * 20.0;
            int have = owned.getOrDefault(js.getSkill().getName().toLowerCase(Locale.ROOT), 0);
            earned += Math.min(have, need) * 20.0;
            if (have < need) {
                missing.add(js.getSkill().getName() + " (needs " + need + "/5, has " + have + "/5)");
            }
        }
        double score = possible == 0 ? 55 : (earned / possible) * 60 + 25; // skills 60% + base 25%
        // academics & eligibility: 15%
        if (job.getMinCgpa() != null && student.getCgpa() != null
                && student.getCgpa().compareTo(job.getMinCgpa()) >= 0) {
            score += 8;
        }
        if ((job.getEligibleBranches() == null || job.getEligibleBranches().isEmpty()
                || job.getEligibleBranches().stream()
                        .anyMatch(b -> b.equalsIgnoreCase(student.getBranch())))
                && (job.getMaxBacklogs() == null || student.getBacklogCount() <= job.getMaxBacklogs())) {
            score += 7;
        }
        BigDecimal finalScore = clamp(score);
        String reasoning = "Heuristic score: " + finalScore
                + "% of the job's required skill weight is covered (" + required.size()
                + " required skills). " + (missing.isEmpty()
                        ? "All required skills are met."
                        : "Gaps: " + String.join("; ", missing) + ".");
        return new MatchResultDto(student.getId(), job.getId(), job.getTitle(),
                finalScore, reasoning, missing, SOURCE_FALLBACK);
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private List<Map.Entry<Skill, Long>> topDemandedSkills(int limit) {
        return jobSkillRepository.countActiveDemandBySkill().stream()
                .map(row -> Map.entry((Skill) row[0], (Long) row[1]))
                .sorted(Map.Entry.<Skill, Long>comparingByValue().reversed())
                .limit(limit)
                .toList();
    }

    private Student requireStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }

    private List<Map<String, Object>> toListOfMaps(JsonNode node) {
        if (node == null || node.isMissingNode() || !node.isArray()) {
            return List.of();
        }
        return objectMapper.convertValue(node, new TypeReference<List<Map<String, Object>>>() {
        });
    }

    private BigDecimal clamp(double score) {
        return BigDecimal.valueOf(Math.min(100, Math.max(0, score))).setScale(2, RoundingMode.HALF_UP);
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private Map<String, Object> linkedMapOf(Object... kv) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            map.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return map;
    }

    /** Stored entity + which engine produced it. */
    public record AnalysisResult(SkillAnalysis analysis, String source) {
    }

    public record RoadmapResult(Roadmap roadmap, String source) {
    }
}
