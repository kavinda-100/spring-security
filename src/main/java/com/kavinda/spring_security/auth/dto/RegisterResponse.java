package com.kavinda.spring_security.auth.dto;

import java.util.UUID;

public record RegisterResponse(
        UUID id,
        String name,
        String email
) {
}
