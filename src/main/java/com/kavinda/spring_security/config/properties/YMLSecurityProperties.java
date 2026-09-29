package com.kavinda.spring_security.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record YMLSecurityProperties(
        String superAdminEmail
) {
}