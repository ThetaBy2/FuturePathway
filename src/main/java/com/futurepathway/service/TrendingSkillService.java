package com.futurepathway.service;

import com.futurepathway.dto.TrendingSkillDto;
import com.futurepathway.entity.Skill;
import com.futurepathway.entity.TrendingSkill;
import com.futurepathway.mapper.TrendingSkillMapper;
import com.futurepathway.repository.JobSkillRepository;
import com.futurepathway.repository.TrendingSkillRepository;
import com.futurepathway.service.ai.GeminiService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrendingSkillService {

    private final TrendingSkillRepository trendingSkillRepository;
    private final JobSkillRepository jobSkillRepository;
    private final GeminiService geminiService;
    private final TrendingSkillMapper trendingSkillMapper;

    @Transactional(readOnly = true)
    public List<TrendingSkillDto> getTrending(int limit) {
        return trendingSkillRepository.findAllByOrderByDemandScoreDesc().stream()
                .limit(Math.max(limit, 1))
                .map(trendingSkillMapper::toDto).toList();
    }

    /** Recomputes demand scores from ACTIVE job postings (spec §4D). */
    @Transactional
    public int refreshScores() {
        List<Object[]> demand = jobSkillRepository.countActiveDemandBySkill();
        for (Object[] row : demand) {
            Skill skill = (Skill) row[0];
            BigDecimal score = BigDecimal.valueOf((Long) row[1]);
            TrendingSkill trending = trendingSkillRepository.findBySkillId(skill.getId())
                    .orElseGet(() -> TrendingSkill.builder().skill(skill).build());
            trending.setDemandScore(score);
            trending.setSource("JOB_POSTINGS");
            trendingSkillRepository.save(trending);
        }
        log.info("Refreshed trending scores for {} skills", demand.size());
        return demand.size();
    }

    /** Asks Gemini for market commentary and stores it on every trending row. */
    @Transactional
    public String refreshInsight() {
        String insight = geminiService.trendingSkillsInsight();
        List<TrendingSkill> all = trendingSkillRepository.findAll();
        all.forEach(t -> t.setInsight(insight));
        trendingSkillRepository.saveAll(all);
        log.info("Refreshed trending insight ({} rows)", all.size());
        return insight;
    }

    @Transactional
    public void refreshAll() {
        refreshScores();
        refreshInsight();
    }
}
