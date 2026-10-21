package com.futurepathway.mapper;

import com.futurepathway.dto.StudentSkillDto;
import com.futurepathway.entity.StudentSkill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentSkillMapper {
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "skill.id", target = "skillId")
    @Mapping(source = "skill.name", target = "skillName")
    @Mapping(source = "skill.category", target = "category")
    StudentSkillDto toDto(StudentSkill studentSkill);
}
