package com.soportehub.backend.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soportehub.backend.user.entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

  boolean existsByOrganizationIdAndEmail(UUID organizationId, String email);

  Optional<UserAccount> findByIdAndOrganizationId(UUID id, UUID organizationId);

}