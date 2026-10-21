package com.futurepathway.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StudentSkillRequest(
        @NotNull Long skillId,
        @NotNull @Min(1) @Max(5) Integer proficiencyLevel) {
}
