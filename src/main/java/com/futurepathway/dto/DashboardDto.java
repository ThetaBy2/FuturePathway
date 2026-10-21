package com.futurepathway.dto;

import java.util.List;

public record DashboardDto(
        PlacementStatsDto stats,
        List<BranchStatDto> byBranch,
        List<CompanyStatDto> byCompany) {
}
