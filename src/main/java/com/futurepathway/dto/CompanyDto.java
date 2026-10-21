package com.futurepathway.dto;

public record CompanyDto(
        Long id,
        Long userId,
        String name,
        String industryDomain,
        String website,
        String hrContactEmail) {
}
