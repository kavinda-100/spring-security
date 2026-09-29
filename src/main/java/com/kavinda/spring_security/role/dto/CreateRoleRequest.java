package com.kavinda.spring_security.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoleRequest(
        @NotBlank(message = "Role name is required")
        @Size(min = 2, max = 100, message = "Role name must be between 2 and 100 characters")
        String name
) {
}
