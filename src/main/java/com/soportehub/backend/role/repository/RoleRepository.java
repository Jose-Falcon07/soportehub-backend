package com.soportehub.backend.role.repository;

import com.soportehub.backend.role.entity.Role;
import com.soportehub.backend.role.entity.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
