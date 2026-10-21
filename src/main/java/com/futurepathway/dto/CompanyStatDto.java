package com.futurepathway.dto;

public record CompanyStatDto(
        String companyName,
        long jobs,
        long applications,
        long selected) {
}
