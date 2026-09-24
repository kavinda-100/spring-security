package com.kavinda.spring_security.auth.security;

import com.kavinda.spring_security.user.entity.AppUser;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class CustomUserDetails implements UserDetails {
    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final boolean enabled;

    public CustomUserDetails(AppUser user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.enabled = user.isEnabled();
    }

    public UUID getId() {
        return id;
    }

    @Override
    @NullMarked
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    @NullMarked
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
