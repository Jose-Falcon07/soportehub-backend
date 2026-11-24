package com.soportehub.backend.organization.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soportehub.backend.organization.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

  List<Category> findByOrganizationIdOrderByName(UUID organizationId);

  Optional<Category> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
