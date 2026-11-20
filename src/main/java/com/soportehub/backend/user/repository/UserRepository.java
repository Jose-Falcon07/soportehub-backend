package com.soportehub.backend.user.repository;

import com.soportehub.backend.role.entity.RoleName;
import com.soportehub.backend.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByOrganizationIdOrderByFullName(UUID organizationId);

    /** Siempre se busca con la empresa: así nunca se encuentra un usuario de otra compañía. */
    Optional<User> findByIdAndOrganizationId(UUID id, UUID organizationId);

    @Query("select distinct u from User u join u.roles r where u.organizationId = :org and r.name = :role "
            + "and u.status = com.soportehub.backend.user.entity.UserStatus.ACTIVE")
    List<User> findActiveByRole(@Param("org") UUID org, @Param("role") RoleName role);
}
