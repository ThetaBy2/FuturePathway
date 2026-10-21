package com.futurepathway.service.ai;

import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Single place where every Gemini prompt is constructed (spec §5).
 * All prompts demand <b>JSON-only</b> output so responses can be parsed
 * deterministically; see README for the full sample prompts.
 */
@Service
public class GeminiPromptService {

    private static final int MAX_INPUT_CHARS = 12_000;

    /** 1. resumeSkillExtraction — CV + project text → strong/weak skills. */
    public String resumeSkillExtractionPrompt(String profileText) {
        return """
                You are a career counsellor analysing a student's placement profile.
                Read the profile below and extract the student's STRONG and WEAK skills.

                PROFILE:
                %s

                Rules:
                - A strong skill must have concrete evidence in the profile (project, certificate, internship).
                - A weak skill is one the profile hints at needing (mentioned but shallow, or commonly expected \
                for the student's branch and missing).
                - level is 1-5 (5 = expert).
                - Summary: 2-3 sentences, honest and encouraging.

                Return JSON ONLY, exactly this shape, no markdown, no commentary:
                {
                  "strong_skills": [{"skill": "string", "level": 1-5, "evidence": "string"}],
                  "weak_skills": [{"skill": "string", "gap": "string", "suggestion": "string"}],
                  "summary": "string"
                }
                """.formatted(clip(profileText));
    }

    /** 2. generateRoadmap — weak skills + target role → ordered milestones. */
    public String roadmapPrompt(String profileText, String targetRole, List<String> weakSkills) {
        return """
                You are a career coach building a 12-week learning roadmap.
                TARGET ROLE: %s
                KNOWN WEAK AREAS: %s

                STUDENT PROFILE:
                %s

                Rules:
                - 6-8 milestones ordered from foundations to job-ready.
                - Each milestone: week range, title, 3-6 concrete tasks, 1-3 free resources (name + URL).
                - Tailor tasks to close the weak areas above for the target role.

                Return JSON ONLY, exactly this shape, no markdown, no commentary:
                {
                  "target_role": "string",
                  "milestones": [
                    {"week": "1-2", "title": "string", "tasks": ["string"], "resources": ["string"]}
                  ]
                }
                """.formatted(targetRole, weakSkills.isEmpty() ? "none identified" : String.join(", ", weakSkills),
                clip(profileText));
    }

    /** 3. computeProfileMatch — student vector + resume vs job → score + reasoning. */
    public String profileMatchPrompt(String profileText, String jobText) {
        return """
                You are a technical recruiter scoring a candidate against a job posting.

                CANDIDATE PROFILE:
                %s

                JOB POSTING:
                %s

                Rules:
                - match_score 0-100: weight required skills (60%%), projects/CV evidence (25%%), \
                academics & eligibility (15%%).
                - reasoning: 3-5 sentences justifying the score.
                - missing_skills: required skills the candidate lacks or is weak in (max 8).

                Return JSON ONLY, exactly this shape, no markdown, no commentary:
                {
                  "match_score": 0-100,
                  "reasoning": "string",
                  "missing_skills": ["string"]
                }
                """.formatted(clip(profileText), clip(jobText));
    }

    /** 4. trendingSkillsInsight — in-demand skills → market commentary. */
    public String trendingInsightPrompt(List<String> topSkillsWithCounts) {
        return """
                You are a labour-market analyst for campus placements in India.
                These skills appear most often in ACTIVE job postings on our portal
                (skill: number of postings):

                %s

                Write a crisp 4-6 sentence insight for students: which skills to prioritise,
                which are rising vs stable, and one practical tip. Plain text inside JSON.

                Return JSON ONLY, exactly this shape, no markdown, no commentary:
                {"insight": "string"}
                """.formatted(String.join("\n", topSkillsWithCounts));
    }

    private String clip(String text) {
        if (text == null) {
            return "(none provided)";
        }
        String t = text.strip();
        if (t.isEmpty()) {
            return "(none provided)";
        }
        return t.length() <= MAX_INPUT_CHARS ? t : t.substring(0, MAX_INPUT_CHARS) + "\n[...truncated]";
    }
}
