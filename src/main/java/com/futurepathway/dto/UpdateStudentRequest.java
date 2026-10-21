package com.futurepathway.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

public record UpdateStudentRequest(
        String name,
        String branch,
        @Min(2000) @Max(2100) Integer batchYear,
        @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal cgpa,
        @Min(0) Integer backlogCount,
        String cvUrl,
        String profilePhotoUrl,
        String resumeText,
        Long collegeId) {
}
