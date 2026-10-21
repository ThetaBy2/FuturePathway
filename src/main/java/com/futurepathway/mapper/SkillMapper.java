package com.futurepathway.mapper;

import com.futurepathway.dto.SkillDto;
import com.futurepathway.dto.SkillRequest;
import com.futurepathway.entity.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    SkillDto toDto(Skill skill);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Skill toEntity(SkillRequest request);
}
