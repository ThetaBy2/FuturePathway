package com.futurepathway.dto;

import com.futurepathway.enums.ApplicationStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record ApplicationDto(
        Long id,
        Long studentId,
        String studentName,
        String rollNo,
        String branch,
        BigDecimal cgpa,
        Long jobId,
        String jobTitle,
        String companyName,
        ApplicationStatus status,
        BigDecimal matchScore,
        String matchReasoning,
        Instant appliedAt) {
}
