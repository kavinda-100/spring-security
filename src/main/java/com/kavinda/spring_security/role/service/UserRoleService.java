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
public class UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Set<GetAllRolesRequest> getAllRoles() {

        return roleRepository.findAll()
                .stream()
                .map(role -> new GetAllRolesRequest(role.getId(), role.getName()))
                .collect(Collectors.toSet());
    }

    @Transactional
    public void updateRoles(UUID userId, Set<UUID> roleIds) {

        AppUser user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id: " + userId)
                );

        Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));

        user.replaceRoles(roles);

        userRepository.save(user);

        eventPublisher.publishEvent(new UserAuthorizationChangedEvent(userId));
    }

}
