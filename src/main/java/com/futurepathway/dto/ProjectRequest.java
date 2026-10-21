package com.futurepathway.dto;

import jakarta.validation.constraints.NotBlank;

public record ProjectRequest(
        @NotBlank String title,
        String description,
        String techStack,
        String repoLink,
        String duration) {
}
