package com.kavinda.spring_security.permission.controller;

import com.kavinda.spring_security.permission.dto.CreateNewPermission;
import com.kavinda.spring_security.permission.dto.GetAllPermissionsRequest;
import com.kavinda.spring_security.permission.dto.GetPermissionRequest;
import com.kavinda.spring_security.permission.dto.UpdatePermissionRequest;
import com.kavinda.spring_security.permission.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    public ResponseEntity<String> createNewPermission(@RequestBody CreateNewPermission request) {
        permissionService.createNewPermission(request);
        return ResponseEntity.ok("Permission created successfully.");
    }

    @GetMapping
    public ResponseEntity<Set<GetAllPermissionsRequest>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    @GetMapping("/{permissionId}")
    public ResponseEntity<GetPermissionRequest> getPermissionById(@PathVariable UUID permissionId) {
        return ResponseEntity.ok(permissionService.getPermissionById(permissionId));
    }

    @PatchMapping("/{roleId}/add-permissions")
    public ResponseEntity<String> updateRolePermissions(@PathVariable UUID roleId, @RequestBody UpdatePermissionRequest permissionIds) {
        permissionService.updateRolePermissions(roleId, permissionIds.permissionIds());
        return ResponseEntity.ok("Role permissions updated successfully.");
    }

    @DeleteMapping("/{permissionId}")
    public ResponseEntity<String> deletePermission(@PathVariable UUID permissionId) {
        permissionService.deletePermission(permissionId);

        return ResponseEntity.noContent().build();
    }
}
