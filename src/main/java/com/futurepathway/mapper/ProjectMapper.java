package com.futurepathway.mapper;

import com.futurepathway.dto.ProjectDto;
import com.futurepathway.dto.ProjectRequest;
import com.futurepathway.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    @Mapping(source = "student.id", target = "studentId")
    ProjectDto toDto(Project project);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Project toEntity(ProjectRequest request);
}
