package com.kavinda.spring_security.auth.dto;

public record CsrfTokenResponse(
        String token,
        String headerName,
        String parameterName
) {
}
