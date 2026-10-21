package com.futurepathway.dto;

import com.futurepathway.enums.SkillCategory;
import java.math.BigDecimal;
import java.time.Instant;

public record TrendingSkillDto(
        Long id,
        Long skillId,
        String skillName,
        SkillCategory category,
        BigDecimal demandScore,
        String source,
        String insight,
        Instant lastUpdated) {
}
