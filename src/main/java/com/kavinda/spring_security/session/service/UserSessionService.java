package com.kavinda.spring_security.session.service;

import com.kavinda.spring_security.auth.security.CustomUserDetails;
import com.kavinda.spring_security.exceptions.types.ResourceNotFoundException;
import com.kavinda.spring_security.user.entity.AppUser;
import com.kavinda.spring_security.user.repostitory.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserRepository userRepository;
    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    public void refreshUserSessions(UUID userId) {

        AppUser user = userRepository
                .findByIdWithAuthorities(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        refreshSessions(sessionRepository, user);
    }

    private <S extends Session> void refreshSessions(FindByIndexNameSessionRepository<S> repository, AppUser user) {

        Map<String, S> sessions = repository.findByPrincipalName(user.getEmail());

        if (sessions.isEmpty()) {
            return;
        }

        CustomUserDetails principal = new CustomUserDetails(user);

        for (S session : sessions.values()) {

            SecurityContext oldContext = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);

            if (oldContext == null) {
                continue;
            }

            Authentication oldAuthentication = oldContext.getAuthentication();

            UsernamePasswordAuthenticationToken newAuthentication = UsernamePasswordAuthenticationToken
                    .authenticated(
                            principal,
                            null,
                            principal.getAuthorities()
                    );

            if (oldAuthentication != null) {
                newAuthentication.setDetails(oldAuthentication.getDetails());
            }

            SecurityContext newContext = SecurityContextHolder.createEmptyContext();

            newContext.setAuthentication(newAuthentication);

            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, newContext);

            repository.save(session);
        }
    }
}
