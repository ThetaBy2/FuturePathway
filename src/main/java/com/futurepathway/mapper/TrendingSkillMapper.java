package com.futurepathway.mapper;

import com.futurepathway.dto.TrendingSkillDto;
import com.futurepathway.entity.TrendingSkill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrendingSkillMapper {
    @Mapping(source = "skill.id", target = "skillId")
    @Mapping(source = "skill.name", target = "skillName")
    @Mapping(source = "skill.category", target = "category")
    TrendingSkillDto toDto(TrendingSkill trendingSkill);
}
