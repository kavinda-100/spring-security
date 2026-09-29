package com.kavinda.spring_security.role.repository;

import com.kavinda.spring_security.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(String name);

    @Query("""
                select distinct r
                from Role r
                join fetch r.permissions p
                where p.id = :permissionId
            """)
    List<Role> findAllByPermissionId(@Param("permissionId") UUID permissionId);
}
