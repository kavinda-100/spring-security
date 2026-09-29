package com.kavinda.spring_security.user.repostitory;

import com.kavinda.spring_security.user.entity.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<AppUser, UUID> {

    // ---
    @EntityGraph(
            attributePaths = {
                    "roles",
                    "roles.permissions"
            }
    )
    Optional<AppUser> findByEmail(String email);

    // ---
    boolean existsByEmail(String email);

    // ---
    @EntityGraph(
            attributePaths = {
                    "roles",
                    "roles.permissions"
            }
    )
    @Query("""
                select u
                from AppUser u
                where u.id = :id
            """)
    Optional<AppUser> findByIdWithAuthorities(@Param("id") UUID id);

    // ---
    @Query("""
                select distinct u.id
                from AppUser u
                join u.roles r
                where r.id = :roleId
            """)
    List<UUID> findUserIdsByRoleId(@Param("roleId") UUID roleId);

    // ---
    @Query("""
                select distinct u.id
                from AppUser u
                join u.roles r
                join r.permissions p
                where p.id = :permissionId
            """)
    List<UUID> findUserIdsByPermissionId(@Param("permissionId") UUID permissionId);

    //----
    @Query("""
                select distinct u
                from AppUser u
                join u.roles r
                where r.id = :roleId
            """)
    List<AppUser> findAllByRoleId(@Param("roleId") UUID roleId);
}
