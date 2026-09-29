package com.kavinda.spring_security.permission.dto;

import java.util.UUID;

public record GetAllPermissionsRequest(
        UUID id,
        String name
) {
}
