package com.kavinda.spring_security.role.events;

import java.util.UUID;

public record UserAuthorizationChangedEvent(UUID userId) {
}