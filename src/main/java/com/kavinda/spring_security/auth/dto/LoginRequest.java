package com.kavinda.spring_security.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email is invalid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 12, message = "Password must be at least 6 characters long and at most 12 characters long")
        String password
) {
}
