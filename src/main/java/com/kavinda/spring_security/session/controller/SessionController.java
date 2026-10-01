package com.kavinda.spring_security.session.controller;

import com.kavinda.spring_security.session.dto.SessionResponse;
import com.kavinda.spring_security.session.service.UserSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/session")
@RequiredArgsConstructor
public class SessionController {

    private final UserSessionService userSessionService;

    /// Get all active sessions for the authenticated user
    ///
    /// @param authentication The authenticated user
    /// @param request        The HTTP request
    /// @return A list of active sessions
    @GetMapping("/active")
    public ResponseEntity<List<SessionResponse>> getActiveSessions(Authentication authentication, HttpServletRequest request) {
        HttpSession currentSession = request.getSession(false);

        var activeSessions = userSessionService.getUserSessions(authentication.getName(), currentSession.getId());
        return ResponseEntity.ok(activeSessions);
    }

    /// Revoke a specific session for the authenticated user
    ///
    /// @param sessionId      The ID of the session to revoke
    /// @param authentication The authenticated user
    /// @return A ResponseEntity with no content
    @DeleteMapping("/revoke/{sessionId}")
    public ResponseEntity<Void> revokeSession(@PathVariable String sessionId, Authentication authentication) {
        userSessionService.revokeSession(authentication.getName(), sessionId);
        return ResponseEntity.noContent().build();
    }

    /// Revoke all other sessions for the authenticated user
    ///
    /// @param authentication The authenticated user
    /// @param request        The HTTP request
    /// @return A ResponseEntity with no content
    @DeleteMapping("/revoke-others")
    public ResponseEntity<Void> revokeOtherSessions(Authentication authentication, HttpServletRequest request) {
        HttpSession currentSession = request.getSession(false);

        userSessionService.revokeOtherSessions(authentication.getName(), currentSession.getId());
        return ResponseEntity.noContent().build();
    }

    /// Revoke all sessions for the authenticated user
    ///
    /// @param authentication The authenticated user
    /// @return A ResponseEntity with no content
    @DeleteMapping("/revoke-all")
    public ResponseEntity<Void> revokeAllSessions(Authentication authentication) {
        userSessionService.revokeAllSessions(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
