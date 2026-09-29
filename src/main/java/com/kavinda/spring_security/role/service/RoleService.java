package com.kavinda.spring_security.role.service;

import com.kavinda.spring_security.exceptions.types.ForbiddenOperationException;
import com.kavinda.spring_security.exceptions.types.ResourceConflictException;
import com.kavinda.spring_security.exceptions.types.ResourceNotFoundException;
import com.kavinda.spring_security.permission.events.RolePermissionsChangedEvent;
import com.kavinda.spring_security.role.dto.CreateRoleRequest;
import com.kavinda.spring_security.role.dto.GetAllRolesRequest;
import com.kavinda.spring_security.role.dto.GetRoleRequest;
import com.kavinda.spring_security.role.dto.RoleResponse;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final Set<String> SYSTEM_ROLES = Set.of("USER", "ADMIN", "SUPER_ADMIN");

    /// Create a new role
    ///
    /// @param request The CreateRoleRequest object containing the name of the role to be created.
    /// @return A RoleResponse object containing the ID and name of the newly created role.
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        String roleName = request.name().trim().toUpperCase();

        if (roleRepository.existsByName(roleName)) {
            throw new ResourceConflictException("Role already exists: " + roleName);
        }

        Role role = Role.builder()
                .name(roleName)
                .build();

        Role savedRole = roleRepository.save(role);

        return new RoleResponse(savedRole.getId(), savedRole.getName());
    }


    /// Get all roles
    ///
    /// @return Set<GetAllRolesRequest>
    public Set<GetAllRolesRequest> getAllRoles() {

        return roleRepository.findAll()
                .stream()
                .map(role -> new GetAllRolesRequest(role.getId(), role.getName()))
                .collect(Collectors.toSet());
    }

    /// Get role by id
    ///
    /// @param roleId The UUID of the role to be retrieved.
    /// @return GetRoleRequest
    public GetRoleRequest getRoleById(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        return new GetRoleRequest(role.getId(), role.getName(), role.getCreatedAt(), role.getUpdatedAt());
    }

    /// added roles for a user
    ///
    /// @param userId  The UUID of the user whose roles are to be updated.
    /// @param roleIds The set of UUIDs representing the roles to be added to the user.
    @Transactional
    public void assignRolesToUser(UUID userId, Set<UUID> roleIds) {

        AppUser user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id: " + userId)
                );

        Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));

        for (Role role : roles) {
            if ("SUPER_ADMIN".equals(role.getName())) {
                throw new ForbiddenOperationException("SUPER_ADMIN cannot be assigned manually");
            }

            user.addRole(role);
        }

        userRepository.save(user);

        eventPublisher.publishEvent(new UserAuthorizationChangedEvent(userId));
    }

    /// Delete a role by its ID
    ///
    /// @param roleId The UUID of the role to be deleted.
    @Transactional
    public void deleteRole(UUID roleId) {

        Role role = roleRepository
                .findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        if (SYSTEM_ROLES.contains(role.getName())) {
            throw new ForbiddenOperationException("System role cannot be deleted: " + role.getName());
        }

        // NOTE: this is expensive operation, consider optimizing it if you have a large number of users
        List<AppUser> affectedUsers = userRepository.findAllByRoleId(roleId);

        List<UUID> affectedUserIds = affectedUsers
                .stream()
                .map(AppUser::getId)
                .toList();

        for (AppUser user : affectedUsers) {
            user.removeRole(role);
        }

        roleRepository.delete(role);

        eventPublisher.publishEvent(new RolePermissionsChangedEvent(affectedUserIds));
    }

}
