package com.kavinda.spring_security.session.dto;

import java.time.Instant;
import java.util.Optional;

public record SessionResponse(
        String id,
        boolean current,
        Instant createdAt,
        Instant lastAccessedAt,
        Instant expiresAt,
        Optional<String> userAgent,
        Optional<String> ipAddress,
        Optional<Instant> loginTime
) {
}
