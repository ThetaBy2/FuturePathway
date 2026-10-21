package com.futurepathway.controller;

import com.futurepathway.dto.DashboardDto;
import com.futurepathway.dto.TrendingSkillDto;
import com.futurepathway.service.DashboardService;
import com.futurepathway.service.TrendingSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Portal", description = "Trending skills & campus dashboard (Module D)")
@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class PortalController {

    private final TrendingSkillService trendingSkillService;
    private final DashboardService dashboardService;

    @Operation(summary = "Top in-demand skills from active postings (all authenticated roles)")
    @GetMapping("/trending")
    @PreAuthorize("hasAnyRole('STUDENT','COLLEGE','COMPANY','ADMIN')")
    public List<TrendingSkillDto> trending(@RequestParam(defaultValue = "10") int limit) {
        return trendingSkillService.getTrending(limit);
    }

    @Operation(summary = "Recompute demand scores from active postings (ADMIN)")
    @PostMapping("/trending/refresh")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> refreshScores() {
        return Map.of("skillsRefreshed", trendingSkillService.refreshScores());
    }

    @Operation(summary = "Regenerate the Gemini market commentary (ADMIN)")
    @PostMapping("/trending/refresh-insight")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> refreshInsight() {
        return Map.of("insight", trendingSkillService.refreshInsight());
    }

    @Operation(summary = "Campus placement dashboard: overall + branch-wise + company-wise")
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('COLLEGE','ADMIN')")
    public DashboardDto dashboard() {
        return dashboardService.dashboard();
    }
}
