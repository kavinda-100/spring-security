package com.kavinda.spring_security.role.controller;

import com.kavinda.spring_security.role.dto.*;
import com.kavinda.spring_security.role.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /// Create a new role
    ///
    /// @param request The CreateRoleRequest object containing the name of the role to be created.
    /// @return A ResponseEntity containing a RoleResponse object representing the newly created role.
    @PostMapping
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return new ResponseEntity<>(roleService.createRole(request), HttpStatus.CREATED);
    }

    /// Get all roles
    ///
    /// @return A ResponseEntity containing a set of GetAllRolesRequest objects representing all roles.
    @GetMapping
    public ResponseEntity<Set<GetAllRolesRequest>> getAllRoles() {
        var roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    /// Get role by id
    ///
    /// @param roleId The UUID of the role to be retrieved.
    /// @return A ResponseEntity containing a GetRoleRequest object representing the role.
    @GetMapping("/{roleId}")
    public ResponseEntity<GetRoleRequest> getRoleById(@PathVariable UUID roleId) {
        var role = roleService.getRoleById(roleId);
        return ResponseEntity.ok(role);
    }

    /// Add new roles to a user
    ///
    /// @param userId  The UUID of the user whose roles are to be added.
    /// @param request An UpdateUserRolesRequest object containing the new role IDs to be assigned to the user.
    /// @return A ResponseEntity containing a success message indicating that the user roles have been updated successfully.
    @PatchMapping("/{userId}/assign-roles")
    public ResponseEntity<String> assignRolesToUser(@PathVariable UUID userId, @RequestBody UpdateUserRolesRequest request) {
        roleService.assignRolesToUser(userId, request.roleIds());
        return ResponseEntity.ok("User roles updated successfully.");
    }

    /// Delete a role by its ID
    ///
    /// @param roleId The UUID of the role to be deleted.
    /// @return A ResponseEntity containing a success message indicating that the role has been deleted successfully.
    @DeleteMapping("/{roleId}")
    public ResponseEntity<String> deleteRole(@PathVariable UUID roleId) {
        roleService.deleteRole(roleId);
        return ResponseEntity.ok("Role deleted successfully.");
    }
}
