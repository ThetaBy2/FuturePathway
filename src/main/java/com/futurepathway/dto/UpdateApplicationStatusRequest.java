package com.futurepathway.dto;

import jakarta.validation.constraints.NotNull;
import com.futurepathway.enums.ApplicationStatus;

public record UpdateApplicationStatusRequest(@NotNull ApplicationStatus status) {
}
