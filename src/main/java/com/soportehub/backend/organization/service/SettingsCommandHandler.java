package com.soportehub.backend.organization.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soportehub.backend.domain.exception.BusinessRuleException;
import com.soportehub.backend.domain.model.Priority;
import com.soportehub.backend.infrastructure.security.TenantContext;
import com.soportehub.backend.organization.entity.Category;
import com.soportehub.backend.organization.entity.SlaPolicy;
import com.soportehub.backend.organization.repository.CategoryRepository;
import com.soportehub.backend.organization.repository.OrganizationRepository;
import com.soportehub.backend.organization.repository.SlaPolicyRepository;

@Service
public class SettingsCommandHandler {

  private static final Set<String> STRATEGIES = Set.of("MANUAL", "ROUND_ROBIN", "LEAST_LOADED");

  private final CategoryRepository categories;
  private final SlaPolicyRepository slaPolicies;
  private final OrganizationRepository organizations;

  public SettingsCommandHandler(CategoryRepository categories, SlaPolicyRepository slaPolicies,
      OrganizationRepository organizations) {
    this.categories = categories;
    this.slaPolicies = slaPolicies;
    this.organizations = organizations;
  }

  @PreAuthorize("hasRole('COMPANY_ADMIN')")
  @Transactional
  public void addCategory(String name) {
    if (name == null || name.isBlank()) {
      throw new BusinessRuleException("El nombre de la categoría es obligatorio");
    }
    categories.save(new Category(TenantContext.require(), name));
  }

  @PreAuthorize("hasRole('COMPANY_ADMIN')")
  @Transactional
  public void saveSla(Priority priority, int hours) {
    if (hours < 1) {
      throw new BusinessRuleException("Las horas deben ser al menos 1");
    }
    UUID org = TenantContext.require();
    slaPolicies.findByOrganizationIdAndPriority(org, priority).ifPresentOrElse(
        policy -> policy.changeHours(hours),
        () -> slaPolicies.save(new SlaPolicy(org, priority, hours)));
  }

  @PreAuthorize("hasRole('COMPANY_ADMIN')")
  @Transactional
  public void changeStrategy(String code) {
    if (!STRATEGIES.contains(code)) {
      throw new BusinessRuleException("Estrategia no válida");
    }
    organizations.findById(TenantContext.require()).orElseThrow().changeStrategy(code);
  }
}
