package com.kavinda.spring_security.config;

import com.kavinda.spring_security.permission.entity.Permission;
import com.kavinda.spring_security.permission.repository.PermissionRepository;
import com.kavinda.spring_security.role.entity.Role;
import com.kavinda.spring_security.role.repository.RoleRepository;
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

            // ----------- set permissions for USER -----------
            Role userRole = roleRepository
                    .findByName("USER")
                    .orElseGet(() ->
                            roleRepository.save(
                                    Role.builder()
                                            .name("USER")
                                            .build()
                            )
                    );

            userRole.setPermissions(Set.of(profileRead, profileUpdate, profileDelete));
            roleRepository.save(userRole);


            // ----------- set permissions for ADMIN -----------
            Role adminRole = roleRepository
                    .findByName("ADMIN")
                    .orElseGet(() ->
                            roleRepository.save(
                                    Role.builder()
                                            .name("ADMIN")
                                            .build()
                            )
                    );

            adminRole.setPermissions(Set.of(profileRead, profileUpdate, profileDelete, userRead, userCreate, userUpdate, userDelete));
            roleRepository.save(adminRole);
        };
    }

    private Permission getOrCreatePermission(PermissionRepository repository, String name) {
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
