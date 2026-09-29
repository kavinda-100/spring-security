package com.kavinda.spring_security.permission.events;

import java.util.List;
import java.util.UUID;

public record RolePermissionsChangedEvent(
        List<UUID> userIds
) {
}
