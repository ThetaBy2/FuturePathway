package com.futurepathway.dto;

import com.futurepathway.enums.SkillCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SkillRequest(
        @NotBlank String name,
        @NotNull SkillCategory category,
        String description) {
}
