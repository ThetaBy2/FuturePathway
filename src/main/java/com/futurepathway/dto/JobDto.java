package com.futurepathway.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record JobDto(
        Long id,
        Long companyId,
        String companyName,
        String title,
        String description,
        String jobDomain,
        BigDecimal minCgpa,
        Integer maxBacklogs,
        List<String> eligibleBranches,
        BigDecimal packageLpa,
        LocalDate deadline,
        boolean active,
        List<JobSkillDto> skills,
        Instant createdAt) {
}
