package com.futurepathway.dto;

import java.time.Instant;

public record ProjectDto(
        Long id,
        Long studentId,
        String title,
        String description,
        String techStack,
        String repoLink,
        String duration,
        Instant createdAt) {
}
