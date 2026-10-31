package com.messwala.backend.dto;

import com.messwala.backend.entity.Role;

public record AuthResponse(
        Long id,
        String name,
        String email,
        Role role,
        String token
) {
}