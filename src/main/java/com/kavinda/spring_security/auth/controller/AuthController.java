package com.kavinda.spring_security.auth.controller;

import com.kavinda.spring_security.auth.dto.LoginRequest;
import com.kavinda.spring_security.auth.dto.LoginResponse;
import com.kavinda.spring_security.auth.dto.RegisterRequest;
import com.kavinda.spring_security.auth.dto.RegisterResponse;
import com.kavinda.spring_security.auth.security.CustomUserDetails;
import com.kavinda.spring_security.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        var response = authService.register(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        var loginResponse = authService.login(loginRequest, request, response);
        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        Map<String, Object> userDetails = Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getUsername(), // username is email in this case
                "enabled", user.isEnabled(),
                "authorities", user.getAuthorities()
        );

        return ResponseEntity.ok(userDetails);
    }

    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> session(HttpServletRequest request, Authentication authentication) {
        HttpSession session = request.getSession(false);

        return ResponseEntity.ok(Map.of(
                "sessionId", session.getId(),
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities()
        ));
    }


}