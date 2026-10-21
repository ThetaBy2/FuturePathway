package com.futurepathway.dto;

import com.futurepathway.validation.PasswordStrength;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterCompanyRequest(
        @NotBlank String contactName,
        @Email @NotBlank String email,
        @PasswordStrength String password,
        @NotBlank String companyName,
        String industryDomain,
        String website,
        @Email String hrContactEmail) {
}
