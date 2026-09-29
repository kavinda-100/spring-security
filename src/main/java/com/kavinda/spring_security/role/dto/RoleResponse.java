package com.kavinda.spring_security.role.dto;

import java.util.UUID;

public record RoleResponse(
        UUID id,
        String name
) {
}
