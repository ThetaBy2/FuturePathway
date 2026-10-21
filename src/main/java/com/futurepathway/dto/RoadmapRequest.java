package com.futurepathway.dto;

import jakarta.validation.constraints.NotBlank;

public record RoadmapRequest(@NotBlank String targetRole) {
}
