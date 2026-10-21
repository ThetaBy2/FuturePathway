package com.futurepathway.mapper;

import com.futurepathway.dto.CompanyDto;
import com.futurepathway.dto.CompanyRequest;
import com.futurepathway.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    @Mapping(source = "user.id", target = "userId")
    CompanyDto toDto(Company company);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Company toEntity(CompanyRequest request);
}
