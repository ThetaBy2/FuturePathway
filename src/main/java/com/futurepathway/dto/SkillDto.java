package com.futurepathway.dto;

import com.futurepathway.enums.SkillCategory;
import java.time.Instant;

public record SkillDto(
        Long id,
        String name,
        SkillCategory category,
        String description,
        Instant createdAt) {
}
