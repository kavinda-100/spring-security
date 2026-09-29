package com.kavinda.spring_security.permission.dto;

import java.time.Instant;
import java.util.UUID;

public record GetPermissionRequest(
        UUID id,
        String name,
        Instant createdAt,
        Instant updatedAt
) {
}
