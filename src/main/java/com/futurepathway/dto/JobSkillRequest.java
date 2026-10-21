package com.futurepathway.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record JobSkillRequest(
        @NotNull Long skillId,
        @Min(1) @Max(5) Integer requiredProficiency) {
}
