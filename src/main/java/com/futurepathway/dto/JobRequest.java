package com.futurepathway.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record JobRequest(
        @NotBlank String title,
        String description,
        String jobDomain,
        @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal minCgpa,
        @Min(0) Integer maxBacklogs,
        List<String> eligibleBranches,
        @DecimalMin("0.0") BigDecimal packageLpa,
        LocalDate deadline,
        Boolean active,
        @Valid List<JobSkillRequest> skills) {
}
