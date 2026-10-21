package com.futurepathway.dto;

import com.futurepathway.enums.SkillCategory;
import com.futurepathway.enums.SkillSource;
import java.time.Instant;

public record StudentSkillDto(
        Long id,
        Long studentId,
        Long skillId,
        String skillName,
        SkillCategory category,
        Integer proficiencyLevel,
        SkillSource source,
        String verifiedBy,
        Instant verifiedAt) {
}
