package com.kavinda.spring_security.permission.dto;

import java.util.Set;
import java.util.UUID;

public record UpdatePermissionRequest(
        Set<UUID> permissionIds
) {
}
