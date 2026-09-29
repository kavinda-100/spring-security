package com.kavinda.spring_security.permission.service;

import com.kavinda.spring_security.exceptions.types.ResourceConflictException;
import com.kavinda.spring_security.exceptions.types.ResourceNotFoundException;
import com.kavinda.spring_security.permission.dto.CreateNewPermission;
import com.kavinda.spring_security.permission.dto.GetAllPermissionsRequest;
import com.kavinda.spring_security.permission.dto.GetPermissionRequest;
import com.kavinda.spring_security.permission.entity.Permission;
import com.kavinda.spring_security.permission.events.RolePermissionsChangedEvent;
import com.kavinda.spring_security.permission.repository.PermissionRepository;
import com.kavinda.spring_security.role.entity.Role;
import com.kavinda.spring_security.role.repository.RoleRepository;
import com.kavinda.spring_security.user.repostitory.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final ApplicationEventPublisher eventPublisher;

    /// Creates a new permission.
    ///
    /// @param request The request object containing the name of the new permission.
    public void createNewPermission(CreateNewPermission request) {

        Optional<Permission> existingPermission = permissionRepository.findByName(request.name());
        if (existingPermission.isPresent()) {
            throw new ResourceConflictException("Permission already exists: " + request.name());
        }

        Permission permission = Permission.builder()
                .name(request.name())
                .build();

        permissionRepository.save(permission);
    }

    /// Retrieves all permissions.
    ///
    /// @return A set of GetAllPermissionsRequest objects representing all permissions.
    public Set<GetAllPermissionsRequest> getAllPermissions() {
        return permissionRepository.findAll()
                .stream()
                .map(permission -> new GetAllPermissionsRequest(permission.getId(), permission.getName()))
                .collect(Collectors.toSet());
    }

    /// Retrieves a permission by its ID.
    ///
    /// @param permissionId The UUID of the permission to retrieve.
    /// @return A GetPermissionRequest object representing the permission.
    public GetPermissionRequest getPermissionById(UUID permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Permission not found with id: " + permissionId)
                );

        return new GetPermissionRequest(permission.getId(), permission.getName(), permission.getCreatedAt(), permission.getUpdatedAt());
    }

    /// add new permissions to a role.
    ///
    /// @param roleId        The UUID of the role to update.
    /// @param permissionIds A set of UUIDs representing the new permissions to associate with the role.
    @Transactional
    public void assignPermissionsToRole(UUID roleId, Set<UUID> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Role not found with id: " + roleId)
                );

        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));

        for (Permission permission : permissions) {
            role.addPermission(permission);
        }

        // NOTE: this will become expensive if the number of users is large. optimization is needed if this becomes a bottleneck.
        List<UUID> affectedUserIds = userRepository.findUserIdsByRoleId(roleId);

        eventPublisher.publishEvent(new RolePermissionsChangedEvent(affectedUserIds));
    }

    /// Deletes a permission by its ID.
    ///
    /// @param permissionId The UUID of the permission to delete.
    @Transactional
    public void deletePermission(UUID permissionId) {

        Permission permission = permissionRepository
                .findById(permissionId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Permission not found with id: " + permissionId)
                );

        // NOTE: this will become expensive if the number of users is large. optimization is needed if this becomes a bottleneck.
        List<UUID> affectedUserIds = userRepository.findUserIdsByPermissionId(permissionId);

        List<Role> affectedRoles = roleRepository.findAllByPermissionId(permissionId);

        for (Role role : affectedRoles) {
            role.removePermission(permission);
        }

        roleRepository.saveAll(affectedRoles);

        permissionRepository.delete(permission);

        eventPublisher.publishEvent(new RolePermissionsChangedEvent(affectedUserIds));
    }
}
