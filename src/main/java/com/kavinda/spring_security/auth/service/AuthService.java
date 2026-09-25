package com.kavinda.spring_security.auth.service;

import com.kavinda.spring_security.auth.dto.LoginRequest;
import com.kavinda.spring_security.auth.dto.LoginResponse;
import com.kavinda.spring_security.auth.dto.RegisterRequest;
import com.kavinda.spring_security.auth.dto.RegisterResponse;
import com.kavinda.spring_security.auth.security.CustomUserDetails;
import com.kavinda.spring_security.exceptions.types.ResourceConflictException;
import com.kavinda.spring_security.exceptions.types.ResourceNotFoundException;
import com.kavinda.spring_security.role.entity.Role;
import com.kavinda.spring_security.role.repository.RoleRepository;
import com.kavinda.spring_security.user.entity.AppUser;
import com.kavinda.spring_security.user.repostitory.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;


    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResourceConflictException(
                    "Email is already registered"
            );
        }

        Role userRole = roleRepository
                .findByName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default USER role does not exist"));

        String passwordHash = passwordEncoder.encode(request.password());

        AppUser user = AppUser.builder()
                .name(request.name().trim())
                .email(email)
                .passwordHash(passwordHash)
                .enabled(true)
                .roles(Set.of(userRole))
                .build();

        AppUser savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail()
        );
    }

    public LoginResponse login(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        Authentication unAuthenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.email(), loginRequest.password());

        Authentication authentication = authenticationManager.authenticate(unAuthenticationRequest);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

        securityContext.setAuthentication(authentication);

        SecurityContextHolder.setContext(securityContext);

        securityContextRepository.saveContext(securityContext, request, response);

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        var authorities = authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                authorities
        );
    }
}
