package com.kavinda.spring_security.auth.service.templates;

import com.kavinda.spring_security.auth.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

public interface IAuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response);

    void changePassword(UUID userId, ChangePasswordRequest request);
}
