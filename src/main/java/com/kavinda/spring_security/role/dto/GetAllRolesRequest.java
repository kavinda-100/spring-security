package com.kavinda.spring_security.role.dto;

import java.util.UUID;

public record GetAllRolesRequest(
        UUID id,
        String name
) {
}
