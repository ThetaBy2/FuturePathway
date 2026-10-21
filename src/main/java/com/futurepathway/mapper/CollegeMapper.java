package com.futurepathway.mapper;

import com.futurepathway.dto.CollegeDto;
import com.futurepathway.dto.CollegeRequest;
import com.futurepathway.entity.College;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CollegeMapper {
    @Mapping(source = "user.id", target = "userId")
    CollegeDto toDto(College college);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    College toEntity(CollegeRequest request);
}
