package com.kavinda.spring_security.permission.service.templates;

import com.kavinda.spring_security.permission.dto.CreateNewPermission;
import com.kavinda.spring_security.permission.dto.GetAllPermissionsRequest;
import com.kavinda.spring_security.permission.dto.GetPermissionRequest;

import java.util.Set;
import java.util.UUID;

public interface IPermissionService {

    void createNewPermission(CreateNewPermission request);

    Set<GetAllPermissionsRequest> getAllPermissions();

    GetPermissionRequest getPermissionById(UUID permissionId);

    void assignPermissionsToRole(UUID roleId, Set<UUID> permissionIds);

    void deletePermission(UUID permissionId);
}
