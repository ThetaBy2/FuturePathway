package com.futurepathway.dto;

import com.futurepathway.enums.VerificationStatus;
import java.time.Instant;
import java.time.LocalDate;

public record CertificateDto(
        Long id,
        Long studentId,
        String title,
        String issuingOrg,
        LocalDate issueDate,
        String credentialUrl,
        VerificationStatus verificationStatus,
        Instant createdAt) {
}
