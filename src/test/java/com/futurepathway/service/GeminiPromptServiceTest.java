package com.futurepathway.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.futurepathway.service.ai.GeminiPromptService;
import java.util.List;
import org.junit.jupiter.api.Test;

class GeminiPromptServiceTest {

    private final GeminiPromptService prompts = new GeminiPromptService();

    @Test
    void resumePrompt_demandsJsonOnlyWithSkillShape() {
        String prompt = prompts.resumeSkillExtractionPrompt("Java developer, built 2 Spring apps.");
        assertTrue(prompt.contains("strong_skills"));
        assertTrue(prompt.contains("weak_skills"));
        assertTrue(prompt.contains("JSON ONLY"));
        assertTrue(prompt.contains("Java developer"));
    }

    @Test
    void roadmapPrompt_includesRoleAndWeaknesses() {
        String prompt = prompts.roadmapPrompt("CSE student", "Backend Developer", List.of("Docker", "SQL"));
        assertTrue(prompt.contains("Backend Developer"));
        assertTrue(prompt.contains("Docker"));
        assertTrue(prompt.contains("milestones"));
        assertTrue(prompt.contains("JSON ONLY"));
    }

    @Test
    void matchPrompt_includesProfileAndJob() {
        String prompt = prompts.profileMatchPrompt("knows Java", "needs Java + Docker");
        assertTrue(prompt.contains("match_score"));
        assertTrue(prompt.contains("missing_skills"));
        assertTrue(prompt.contains("needs Java + Docker"));
    }

    @Test
    void trendingPrompt_listsSkills() {
        String prompt = prompts.trendingInsightPrompt(List.of("- Java: 12 active postings"));
        assertTrue(prompt.contains("insight"));
        assertTrue(prompt.contains("Java: 12 active postings"));
    }
}
