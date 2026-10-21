package com.futurepathway.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CertificateRequest(
        @NotBlank String title,
        String issuingOrg,
        LocalDate issueDate,
        String credentialUrl) {
}
