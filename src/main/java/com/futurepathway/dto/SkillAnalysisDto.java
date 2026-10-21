package com.futurepathway.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record SkillAnalysisDto(
        Long id,
        Long studentId,
        List<Map<String, Object>> strongSkills,
        List<Map<String, Object>> weakSkills,
        String summaryText,
        Instant generatedAt,
        String source) {
}
