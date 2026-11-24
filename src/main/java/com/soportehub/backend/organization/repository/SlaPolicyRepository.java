package com.soportehub.backend.organization.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soportehub.backend.domain.model.Priority;
import com.soportehub.backend.organization.entity.SlaPolicy;

public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, UUID> {

  List<SlaPolicy> findByOrganizationId(UUID organizationId);

  Optional<SlaPolicy> findByOrganizationIdAndPriority(UUID organizationId, Priority priority);
}
