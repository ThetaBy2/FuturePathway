package com.futurepathway.dto;

import com.futurepathway.enums.Role;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String name,
        String email,
        Role role,
        Long profileId) {
    public AuthResponse(String token, Long userId, String name, String email, Role role, Long profileId) {
        this(token, "Bearer", userId, name, email, role, profileId);
    }
}
