package com.kavinda.spring_security.role.listeners;

import com.kavinda.spring_security.role.events.UserAuthorizationChangedEvent;
import com.kavinda.spring_security.session.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserAuthorizationChangedListener {

    private final UserSessionService userSessionService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAuthorizationChanged(UserAuthorizationChangedEvent event) {
        userSessionService.refreshUserSessions(event.userId());
    }
}
