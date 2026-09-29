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

    /// Register a new user
    ///
    /// @param request The registration request containing user details
    /// @return A ResponseEntity containing the registration response and HTTP status code
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        var response = authService.register(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /// Login an existing user
    ///
    /// @param loginRequest The login request containing user credentials
    /// @param request      The HttpServletRequest object for the current request
    /// @param response     The HttpServletResponse object for the current response
    /// @return A ResponseEntity containing the login response and HTTP status code
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        var loginResponse = authService.login(loginRequest, request, response);
        return ResponseEntity.ok(loginResponse);
    }

    /// Get the details of the currently authenticated user
    ///
    /// @param authentication The Authentication object containing the details of the currently authenticated user
    /// @return A ResponseEntity containing a map of user details and HTTP status code
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

    /// Get the current session details
    ///
    /// @param request        The HttpServletRequest object for the current request
    /// @param authentication The Authentication object containing the details of the currently authenticated user
    /// @return A ResponseEntity containing a map of session details and HTTP status code
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