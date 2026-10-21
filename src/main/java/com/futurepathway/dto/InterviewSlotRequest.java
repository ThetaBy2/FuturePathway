package com.futurepathway.dto;

import com.futurepathway.enums.SlotStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record InterviewSlotRequest(
        @NotNull Long studentId,
        @NotBlank String roundName,
        @NotNull Instant scheduledAt,
        String venueOrLink,
        SlotStatus status) {
}
