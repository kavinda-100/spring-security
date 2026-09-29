package com.kavinda.spring_security.permission.controller;

import com.kavinda.spring_security.permission.dto.CreateNewPermission;
import com.kavinda.spring_security.permission.dto.GetAllPermissionsRequest;
import com.kavinda.spring_security.permission.dto.GetPermissionRequest;
import com.kavinda.spring_security.permission.dto.UpdatePermissionRequest;
import com.kavinda.spring_security.permission.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /// Creates a new permission based on the provided request.
    ///
    /// @param request The request body containing the details of the new permission to be created.
    /// @return A ResponseEntity containing a success message if the permission is created successfully.
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<String> createNewPermission(@RequestBody CreateNewPermission request) {
        permissionService.createNewPermission(request);
        return ResponseEntity.ok("Permission created successfully.");
    }

    /// Retrieves all permissions available in the system.
    ///
    /// @return A ResponseEntity containing a set of GetAllPermissionsRequest objects representing all permissions.
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<Set<GetAllPermissionsRequest>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    /// Retrieves a specific permission by its unique identifier.
    ///
    /// @param permissionId The unique identifier of the permission to be retrieved.
    /// @return A ResponseEntity containing the GetPermissionRequest object representing the requested permission.
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/{permissionId}")
    public ResponseEntity<GetPermissionRequest> getPermissionById(@PathVariable UUID permissionId) {
        return ResponseEntity.ok(permissionService.getPermissionById(permissionId));
    }

    /// add new permissions to a specific role.
    ///
    /// @param roleId        The unique identifier of the role whose permissions are to be updated.
    /// @param permissionIds The request body containing the list of permission IDs to be associated with the role.
    /// @return A ResponseEntity containing a success message if the role permissions are updated successfully.
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping("/{roleId}/assign-permissions")
    public ResponseEntity<String> addPermissionsToRole(@PathVariable UUID roleId, @RequestBody UpdatePermissionRequest permissionIds) {
        permissionService.assignPermissionsToRole(roleId, permissionIds.permissionIds());
        return ResponseEntity.ok("Role permissions updated successfully.");
    }

    /// Deletes a specific permission by its unique identifier.
    ///
    /// @param permissionId The unique identifier of the permission to be deleted.
    /// @return A ResponseEntity with no content if the permission is deleted successfully.
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{permissionId}")
    public ResponseEntity<String> deletePermission(@PathVariable UUID permissionId) {
        permissionService.deletePermission(permissionId);

        return ResponseEntity.noContent().build();
    }
}
