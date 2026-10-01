package com.kavinda.spring_security.session.service.templates;

import com.kavinda.spring_security.session.dto.SessionResponse;

import java.util.List;
import java.util.UUID;

public interface IUserSessionService {

    void refreshUserSessions(UUID userId);

    List<SessionResponse> getUserSessions(String principalName, String currentSessionId);

    void revokeSession(String principalName, String sessionId);

    void revokeOtherSessions(String principalName, String currentSessionId);

    void revokeAllSessions(String principalName);
}
