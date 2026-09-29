package com.kavinda.spring_security.permission.listeners;

import com.kavinda.spring_security.permission.events.RolePermissionsChangedEvent;
import com.kavinda.spring_security.session.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RolePermissionsChangedListener {

    private final UserSessionService userSessionService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRolePermissionsChanged(RolePermissionsChangedEvent event) {

        // NOTE: This will become expensive if the number of users is large. optimization is needed if this becomes a bottleneck.
        for (UUID userId : event.userIds()) {
            userSessionService.refreshUserSessions(userId);
        }
    }
}
