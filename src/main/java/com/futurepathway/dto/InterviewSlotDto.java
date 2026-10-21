package com.futurepathway.dto;

import com.futurepathway.enums.SlotStatus;
import java.time.Instant;

public record InterviewSlotDto(
        Long id,
        Long processId,
        Long jobId,
        String jobTitle,
        Long studentId,
        String studentName,
        String roundName,
        Instant scheduledAt,
        String venueOrLink,
        SlotStatus status) {
}
