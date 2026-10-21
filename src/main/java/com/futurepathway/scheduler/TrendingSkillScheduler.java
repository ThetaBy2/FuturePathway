package com.futurepathway.scheduler;

import com.futurepathway.service.TrendingSkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Nightly refresh of trending-skill scores + Gemini commentary. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrendingSkillScheduler {

    private final TrendingSkillService trendingSkillService;

    @Scheduled(cron = "${app.scheduler.trending-cron:0 0 2 * * *}")
    public void refreshTrendingSkills() {
        log.info("Scheduled trending-skills refresh started");
        trendingSkillService.refreshAll();
        log.info("Scheduled trending-skills refresh finished");
    }
}
