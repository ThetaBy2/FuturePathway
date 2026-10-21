package com.futurepathway.dto;

import java.math.BigDecimal;
import java.util.List;

public record MatchResultDto(
        Long studentId,
        Long jobId,
        String jobTitle,
        BigDecimal matchScore,
        String reasoning,
        List<String> missingSkills,
        String source) {
}
