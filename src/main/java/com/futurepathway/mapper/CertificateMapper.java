package com.futurepathway.mapper;

import com.futurepathway.dto.CertificateDto;
import com.futurepathway.dto.CertificateRequest;
import com.futurepathway.entity.Certificate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CertificateMapper {
    @Mapping(source = "student.id", target = "studentId")
    CertificateDto toDto(Certificate certificate);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "verificationStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Certificate toEntity(CertificateRequest request);
}
