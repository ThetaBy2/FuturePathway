package com.futurepathway.dto;

import com.futurepathway.enums.Role;
import java.time.Instant;

public record UserDto(
        Long id,
        String name,
        String email,
        Role role,
        boolean enabled,
        Instant createdAt) {
}
