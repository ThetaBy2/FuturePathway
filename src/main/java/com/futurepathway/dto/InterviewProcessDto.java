package com.futurepathway.dto;

import java.util.List;
import java.util.Map;

public record InterviewProcessDto(
        Long id,
        Long jobId,
        String jobTitle,
        List<Map<String, Object>> rounds,
        Map<String, Object> selectionCriteria) {
}
