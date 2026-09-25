package com.kavinda.spring_security.auth.dto;

import java.util.Collection;
import java.util.UUID;

public record LoginResponse(
        UUID id,
        String email,
        Collection<String> authorities
) {
}
