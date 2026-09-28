package com.kavinda.spring_security.role.controller;

import com.kavinda.spring_security.role.dto.GetAllRolesRequest;
import com.kavinda.spring_security.role.dto.UpdateUserRolesRequest;
import com.kavinda.spring_security.role.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final UserRoleService userRoleService;

    @GetMapping
    public ResponseEntity<Set<GetAllRolesRequest>> getAllRoles() {
        var roles = userRoleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @PatchMapping("/{userId}/add-roles")
    public ResponseEntity<String> updateUserRoles(@PathVariable UUID userId, @RequestBody UpdateUserRolesRequest request) {
        userRoleService.updateRoles(userId, request.roleIds());
        return ResponseEntity.ok("User roles updated successfully.");
    }
}
