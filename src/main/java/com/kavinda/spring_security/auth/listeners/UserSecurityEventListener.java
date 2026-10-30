package com.kavinda.spring_security.auth.listeners;

import com.kavinda.spring_security.auth.events.UserCredentialsChangedEvent;
import com.kavinda.spring_security.session.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserSecurityEventListener {

    private final UserSessionService userSessionService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCredentialsChanged(UserCredentialsChangedEvent event) {
        userSessionService.revokeAllSessions(event.email());
    }
}
