package com.futurepathway.dto;

import com.futurepathway.validation.PasswordStrength;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterAdminRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @PasswordStrength String password) {
}
