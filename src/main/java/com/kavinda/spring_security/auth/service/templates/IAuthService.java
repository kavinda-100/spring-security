package com.kavinda.spring_security.auth.service.templates;

import com.kavinda.spring_security.auth.dto.LoginRequest;
import com.kavinda.spring_security.auth.dto.LoginResponse;
import com.kavinda.spring_security.auth.dto.RegisterRequest;
import com.kavinda.spring_security.auth.dto.RegisterResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface IAuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response);
}
