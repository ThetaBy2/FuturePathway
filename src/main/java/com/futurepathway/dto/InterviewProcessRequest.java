package com.futurepathway.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public record InterviewProcessRequest(
        @NotNull List<Map<String, Object>> rounds,
        Map<String, Object> selectionCriteria) {
}
