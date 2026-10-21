package com.futurepathway.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CompanyRequest(
        @NotBlank String name,
        String industryDomain,
        String website,
        @Email String hrContactEmail) {
}
