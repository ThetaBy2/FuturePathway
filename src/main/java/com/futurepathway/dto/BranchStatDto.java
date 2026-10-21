package com.futurepathway.dto;

public record BranchStatDto(
        String branch,
        long students,
        long applications,
        long selected) {
}
