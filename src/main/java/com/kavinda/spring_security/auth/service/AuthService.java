package com.kavinda.spring_security.auth.service;

import com.kavinda.spring_security.auth.dto.RegisterRequest;
import com.kavinda.spring_security.auth.dto.RegisterResponse;
import com.kavinda.spring_security.exceptions.types.ResourceConflictException;
import com.kavinda.spring_security.exceptions.types.ResourceNotFoundException;
import com.kavinda.spring_security.role.entity.Role;
import com.kavinda.spring_security.role.repository.RoleRepository;
import com.kavinda.spring_security.user.entity.AppUser;
import com.kavinda.spring_security.user.repostitory.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


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
}
