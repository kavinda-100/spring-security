package com.kavinda.spring_security.role.service.templates;

import com.kavinda.spring_security.role.dto.CreateRoleRequest;
import com.kavinda.spring_security.role.dto.GetAllRolesRequest;
import com.kavinda.spring_security.role.dto.GetRoleRequest;
import com.kavinda.spring_security.role.dto.RoleResponse;

import java.util.Set;
import java.util.UUID;

public interface IRoleService {

    RoleResponse createRole(CreateRoleRequest request);

    Set<GetAllRolesRequest> getAllRoles();

    GetRoleRequest getRoleById(UUID roleId);

    void assignRolesToUser(UUID userId, Set<UUID> roleIds);

    void deleteRole(UUID roleId);
}
