package com.kavinda.spring_security.role.service;

import com.kavinda.spring_security.exceptions.types.ResourceNotFoundException;
import com.kavinda.spring_security.role.dto.GetAllRolesRequest;
import com.kavinda.spring_security.role.entity.Role;
import com.kavinda.spring_security.role.events.UserAuthorizationChangedEvent;
import com.kavinda.spring_security.role.repository.RoleRepository;
import com.kavinda.spring_security.user.entity.AppUser;
import com.kavinda.spring_security.user.repostitory.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ApplicationEventPublisher eventPublisher;


    /// Get all roles
    ///
    /// @return Set<GetAllRolesRequest>
    public Set<GetAllRolesRequest> getAllRoles() {

        return roleRepository.findAll()
                .stream()
                .map(role -> new GetAllRolesRequest(role.getId(), role.getName()))
                .collect(Collectors.toSet());
    }

    /// added roles for a user
    ///
    /// @param userId  The UUID of the user whose roles are to be updated.
    /// @param roleIds The set of UUIDs representing the roles to be added to the user.
    @Transactional
    public void updateRoles(UUID userId, Set<UUID> roleIds) {

        AppUser user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id: " + userId)
                );

        Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));

        for (Role role : roles) {
            user.addRole(role);
        }

        userRepository.save(user);

        eventPublisher.publishEvent(new UserAuthorizationChangedEvent(userId));
    }

}
