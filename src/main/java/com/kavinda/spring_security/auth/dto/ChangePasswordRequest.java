package com.kavinda.spring_security.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required")
        @Size(min = 6, max = 12, message = "Current password must be between 6 and 12 characters long")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 6, max = 12, message = "New password must be between 6 and 12 characters long")
        String newPassword
) {
}
