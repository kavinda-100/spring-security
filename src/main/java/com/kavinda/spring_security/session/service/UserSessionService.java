package com.kavinda.spring_security.session.service;

import com.kavinda.spring_security.auth.security.CustomUserDetails;
import com.kavinda.spring_security.exceptions.types.ResourceNotFoundException;
import com.kavinda.spring_security.session.constants.SessionAttributes;
import com.kavinda.spring_security.session.dto.SessionResponse;
import com.kavinda.spring_security.session.service.templates.IUserSessionService;
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

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserSessionService implements IUserSessionService {

    private final UserRepository userRepository;
    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    /// Refreshes all sessions of a user with the latest authorities and details.
    ///
    /// @param userId The ID of the user whose sessions need to be refreshed.
    @Override
    public void refreshUserSessions(UUID userId) {

        AppUser user = userRepository
                .findByIdWithAuthorities(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        refreshSessions(sessionRepository, user);
    }

    /// Retrieves all active sessions for a given user, including the current session.
    ///
    /// @param principalName    The principal name (usually the username or email) of the user whose sessions are to be retrieved.
    /// @param currentSessionId The ID of the current session, which can be used to identify and highlight the current session in the response.
    /// @return A list of SessionResponse objects representing the user's active sessions.
    @Override
    public List<SessionResponse> getUserSessions(String principalName, String currentSessionId) {
        return getUserSessions(sessionRepository, principalName, currentSessionId);
    }

    /// Revokes a specific session for a user based on the provided principal name and session ID.
    ///
    /// @param principalName The principal name (usually the username or email) of the user whose session is to be revoked.
    /// @param sessionId     The ID of the session to be revoked.
    @Override
    public void revokeSession(String principalName, String sessionId) {
        revokeSession(sessionRepository, principalName, sessionId);
    }

    /// Revokes all sessions for a user except for the current session, effectively logging out the user from all other devices or browsers.
    ///
    /// @param principalName    The principal name (usually the username or email) of the user whose other sessions are to be revoked.
    /// @param currentSessionId The ID of the current session, which will be preserved while all other sessions are revoked.
    @Override
    public void revokeOtherSessions(String principalName, String currentSessionId) {
        revokeOtherSessions(sessionRepository, principalName, currentSessionId);
    }

    /// Revokes all sessions for a user, effectively logging out the user from all devices or browsers.
    ///
    /// @param principalName The principal name (usually the username or email) of the user whose sessions are to be revoked.
    @Override
    public void revokeAllSessions(String principalName) {
        revokeAllSessions(sessionRepository, principalName);
    }

    // ---------------------------- private methods ------------------------------------------

    /// Helper method to revoke all sessions for a user based on the provided principal name.
    ///
    /// @param repository    The session repository used to manage sessions.
    /// @param principalName The principal name (usually the username or email) of the user whose sessions are to be revoked.
    /// @param <S>           The type of session being managed, extending the Session interface.
    private <S extends Session> void revokeAllSessions(FindByIndexNameSessionRepository<S> repository, String principalName) {
        Map<String, S> sessions = repository.findByPrincipalName(principalName);

        for (String sessionId : sessions.keySet()) {
            repository.deleteById(sessionId);
        }
    }

    /// Helper method to revoke all sessions for a user except for the current session.
    ///
    /// @param repository       The session repository used to manage sessions.
    /// @param principalName    The principal name (usually the username or email) of the user whose other sessions are to be revoked.
    /// @param currentSessionId The ID of the current session, which will be preserved while all other sessions are revoked.
    /// @param <S>              The type of session being managed, extending the Session interface.
    private <S extends Session> void revokeOtherSessions(
            FindByIndexNameSessionRepository<S> repository,
            String principalName,
            String currentSessionId
    ) {
        Map<String, S> sessions = repository.findByPrincipalName(principalName);

        for (String sessionId : sessions.keySet()) {
            // Skip the current session
            if (sessionId.equals(currentSessionId)) {
                continue;
            }

            repository.deleteById(sessionId);
        }
    }

    /// Helper method to revoke a specific session for a user based on the provided principal name and session ID.
    ///
    /// @param repository    The session repository used to manage sessions.
    /// @param principalName The principal name (usually the username or email) of the user whose session is to be revoked.
    /// @param sessionId     The ID of the session to be revoked.
    /// @param <S>           The type of session being managed, extending the Session interface.
    private <S extends Session> void revokeSession(FindByIndexNameSessionRepository<S> repository, String principalName, String sessionId) {
        Map<String, S> sessions = repository.findByPrincipalName(principalName);

        if (!sessions.containsKey(sessionId)) {
            throw new ResourceNotFoundException("Session not found");
        }

        repository.deleteById(sessionId);
    }

    /// Helper method to retrieve all active sessions for a given user, including the current session.
    ///
    /// @param repository       The session repository used to manage sessions.
    /// @param principalName    The principal name (usually the username or email) of the user whose sessions are to be retrieved.
    /// @param currentSessionId The ID of the current session, which will be included in the list of active sessions.
    /// @param <S>              The type of session being managed, extending the Session interface.
    /// @return A list of active sessions for the specified user.
    private <S extends Session> List<SessionResponse> getUserSessions(
            FindByIndexNameSessionRepository<S> repository,
            String principalName,
            String currentSessionId
    ) {
        return repository
                .findByPrincipalName(principalName)
                .values()
                .stream()
                .map(session -> toResponse(
                        session,
                        currentSessionId
                ))
                .sorted(Comparator.comparing(SessionResponse::lastAccessedAt).reversed())
                .toList();
    }

    /// Converts a Session object into a SessionResponse object, which contains relevant session information for the client.
    ///
    /// @param session          The Session object to be converted into a SessionResponse.
    /// @param currentSessionId The ID of the current session, used to determine if the session being converted is the current session.
    /// @return A SessionResponse object containing relevant session information for the client.
    private SessionResponse toResponse(Session session, String currentSessionId) {
        Instant expiresAt = session.getLastAccessedTime().plus(session.getMaxInactiveInterval());

        return new SessionResponse(
                session.getId(),
                session.getId().equals(currentSessionId),
                session.getCreationTime(),
                session.getLastAccessedTime(),
                expiresAt,
                Optional.ofNullable(session.getAttribute(SessionAttributes.USER_AGENT)),
                Optional.ofNullable(session.getAttribute(SessionAttributes.IP_ADDRESS)),
                Optional.ofNullable(session.getAttribute(SessionAttributes.LOGIN_TIME))

        );
    }

    /// Helper method to refresh all sessions of a user with the latest authorities and details. This is useful when a user's roles or permissions have changed, and you want to ensure that all active sessions reflect those changes.
    ///
    /// @param repository The session repository used to manage sessions.
    /// @param user       The AppUser object representing the user whose sessions need to be refreshed.
    /// @param <S>        The type of session being managed, extending the Session interface.
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
