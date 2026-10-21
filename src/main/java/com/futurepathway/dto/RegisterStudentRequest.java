package com.futurepathway.dto;

import com.futurepathway.validation.PasswordStrength;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record RegisterStudentRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @PasswordStrength String password,
        @NotBlank String rollNo,
        @NotBlank String branch,
        @NotNull @Min(2000) @Max(2100) Integer batchYear,
        @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal cgpa,
        @Min(0) Integer backlogCount,
        Long collegeId,
        String cvUrl,
        String resumeText) {
}
