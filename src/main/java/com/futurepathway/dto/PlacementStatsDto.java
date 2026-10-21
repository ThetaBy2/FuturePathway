package com.futurepathway.dto;

import java.util.Map;

public record PlacementStatsDto(
        long totalStudents,
        long totalCompanies,
        long totalJobs,
        long activeJobs,
        long totalApplications,
        Map<String, Long> byStatus) {
}
