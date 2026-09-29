package com.kavinda.spring_security.config;

import com.kavinda.spring_security.permission.entity.Permission;
import com.kavinda.spring_security.permission.repository.PermissionRepository;
import com.kavinda.spring_security.role.entity.Role;
import com.kavinda.spring_security.role.repository.RoleRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class RoleAndPermissionInitializer {

    @Bean
    CommandLineRunner initializeSecurityData(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        return args -> {

            // ----------- permission creation -----------
            // ----------- profile permissions -----------
            Permission profileRead = getOrCreatePermission(permissionRepository, "profile:read");
            Permission profileUpdate = getOrCreatePermission(permissionRepository, "profile:update");
            Permission profileDelete = getOrCreatePermission(permissionRepository, "profile:delete");
            // ----------- user permissions -----------
            Permission userRead = getOrCreatePermission(permissionRepository, "user:read");
            Permission userCreate = getOrCreatePermission(permissionRepository, "user:create");
            Permission userUpdate = getOrCreatePermission(permissionRepository, "user:update");
            Permission userDelete = getOrCreatePermission(permissionRepository, "user:delete");
            // ----------- role permissions -----------
            Permission roleRead = getOrCreatePermission(permissionRepository, "role:read");
            Permission roleCreate = getOrCreatePermission(permissionRepository, "role:create");
            Permission roleUpdate = getOrCreatePermission(permissionRepository, "role:assign");
            Permission roleDelete = getOrCreatePermission(permissionRepository, "role:delete");
            // ----------- permission permissions -----------
            Permission permissionRead = getOrCreatePermission(permissionRepository, "permission:read");
            Permission permissionCreate = getOrCreatePermission(permissionRepository, "permission:create");
            Permission permissionUpdate = getOrCreatePermission(permissionRepository, "permission:assign");
            Permission permissionDelete = getOrCreatePermission(permissionRepository, "permission:delete");

            // ----------- set permissions for USER -----------
            Role userRole = getOrCreateRole(roleRepository, "USER");
            userRole.setPermissions(Set.of(profileRead, profileUpdate, profileDelete));
            roleRepository.save(userRole);


            // ----------- set permissions for ADMIN -----------
            Role adminRole = getOrCreateRole(roleRepository, "ADMIN");
            adminRole.setPermissions(Set.of(profileRead, profileUpdate, profileDelete, userRead, userCreate, userUpdate, userDelete));
            roleRepository.save(adminRole);

            // ----------- set permissions for SUPER_ADMIN -----------
            Role superAdminRole = getOrCreateRole(roleRepository, "SUPER_ADMIN");
            superAdminRole.setPermissions(
                    Set.of(profileRead, profileUpdate, profileDelete, userRead, userCreate, userUpdate, userDelete,
                            roleRead, roleCreate, roleUpdate, roleDelete, permissionRead, permissionCreate, permissionUpdate, permissionDelete)
            );
            roleRepository.save(superAdminRole);
        };
    }

    /// Get or create a role by name.
    ///
    /// @param roleRepository role repository
    /// @param roleName       role name (e.g., "USER", "ADMIN", "SUPER_ADMIN")
    /// @return the existing or newly created role
    /// @implSpec IMPORTANT: Role name must be unique and should be in uppercase.
    private static @NonNull Role getOrCreateRole(RoleRepository roleRepository, String roleName) {
        return roleRepository
                .findByName(roleName)
                .orElseGet(() ->
                        roleRepository.save(
                                Role.builder()
                                        .name(roleName)
                                        .build()
                        )
                );
    }

    /// Get or create a permission by name.
    ///
    /// @param repository permission repository
    /// @param name       permission name
    /// @return the existing or newly created permission
    /// @implSpec IMPORTANT: Permission name must be unique.
    private static @NonNull Permission getOrCreatePermission(PermissionRepository repository, String name) {
        return repository
                .findByName(name)
                .orElseGet(() ->
                        repository.save(
                                Permission.builder()
                                        .name(name)
                                        .build()
                        )
                );
    }
}
