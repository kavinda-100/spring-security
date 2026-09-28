package com.kavinda.spring_security.role.dto;

import java.util.Set;
import java.util.UUID;

public record UpdateUserRolesRequest(
        Set<UUID> roleIds
) {
}
