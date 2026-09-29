package com.kavinda.spring_security.role.dto;

import java.time.Instant;
import java.util.UUID;

public record GetRoleRequest(
        UUID id,
        String name,
        Instant createdAt,
        Instant updatedAt
) {
}
