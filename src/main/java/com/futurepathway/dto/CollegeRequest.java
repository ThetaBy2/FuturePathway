package com.futurepathway.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CollegeRequest(
        @NotBlank String name,
        String address,
        @Email String tpoContactEmail) {
}
