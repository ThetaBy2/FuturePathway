package com.futurepathway.controller;

import com.futurepathway.dto.MatchResultDto;
import com.futurepathway.dto.RoadmapDto;
import com.futurepathway.dto.RoadmapRequest;
import com.futurepathway.dto.SkillAnalysisDto;
import com.futurepathway.security.CurrentUser;
import com.futurepathway.service.RoadmapService;
import com.futurepathway.service.SkillAnalysisService;
import com.futurepathway.service.StudentService;
import com.futurepathway.service.ai.GeminiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI (Gemini)", description = "Profile analysis, roadmaps & match scoring (spec §5)")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final SkillAnalysisService skillAnalysisService;
    private final RoadmapService roadmapService;
    private final StudentService studentService;
    private final GeminiService geminiService;

    @Operation(summary = "Analyse my profile → strong/weak skills (STUDENT)")
    @PostMapping("/analyze-me")
    @PreAuthorize("hasRole('STUDENT')")
    public SkillAnalysisDto analyzeMe() {
        return skillAnalysisService.analyzeMe(CurrentUser.email());
    }

    @Operation(summary = "Get my latest analysis (STUDENT)")
    @GetMapping("/analysis/me")
    @PreAuthorize("hasRole('STUDENT')")
    public SkillAnalysisDto latestAnalysis() {
        return skillAnalysisService.latest(CurrentUser.email());
    }

    @Operation(summary = "My analysis history — progress over time (STUDENT)")
    @GetMapping("/analysis/me/history")
    @PreAuthorize("hasRole('STUDENT')")
    public List<SkillAnalysisDto> analysisHistory() {
        return skillAnalysisService.history(CurrentUser.email());
    }

    @Operation(summary = "Generate a personalised roadmap for a target role (STUDENT)")
    @PostMapping("/roadmap")
    @PreAuthorize("hasRole('STUDENT')")
    public RoadmapDto generateRoadmap(@Valid @RequestBody RoadmapRequest request) {
        return roadmapService.generate(CurrentUser.email(), request.targetRole());
    }

    @Operation(summary = "My roadmaps (STUDENT)")
    @GetMapping("/roadmaps/me")
    @PreAuthorize("hasRole('STUDENT')")
    public List<RoadmapDto> myRoadmaps() {
        return roadmapService.history(CurrentUser.email());
    }

    @Operation(summary = "Score my fit for a job + gap analysis (STUDENT)")
    @GetMapping("/match/{jobId}")
    @PreAuthorize("hasRole('STUDENT')")
    public MatchResultDto myMatch(@PathVariable Long jobId) {
        Long studentId = studentService.requireStudentByEmail(CurrentUser.email()).getId();
        return geminiService.computeProfileMatch(studentId, jobId);
    }

    @Operation(summary = "Score any student's fit for a job (COMPANY/COLLEGE/ADMIN)")
    @GetMapping("/students/{studentId}/match/{jobId}")
    @PreAuthorize("hasAnyRole('COMPANY','COLLEGE','ADMIN')")
    public MatchResultDto candidateMatch(@PathVariable Long studentId, @PathVariable Long jobId) {
        return geminiService.computeProfileMatch(studentId, jobId);
    }
}
