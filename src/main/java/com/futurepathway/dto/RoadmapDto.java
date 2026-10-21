package com.futurepathway.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record RoadmapDto(
        Long id,
        Long studentId,
        String targetRole,
        List<Map<String, Object>> milestones,
        Instant generatedAt,
        String source) {
}
