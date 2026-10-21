package com.futurepathway.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record StudentDto(
        Long id,
        Long userId,
        String name,
        String email,
        String rollNo,
        String branch,
        Integer batchYear,
        BigDecimal cgpa,
        Integer backlogCount,
        String cvUrl,
        String profilePhotoUrl,
        Long collegeId,
        String collegeName,
        Instant createdAt,
        Instant updatedAt) {
}
