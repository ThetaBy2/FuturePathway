package com.futurepathway.dto;

import jakarta.validation.constraints.NotNull;

public record ApprovalRequest(@NotNull Boolean approved) {
}
