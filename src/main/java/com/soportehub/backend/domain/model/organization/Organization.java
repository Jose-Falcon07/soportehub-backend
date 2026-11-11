package com.soportehub.backend.domain.model.organization;

import java.time.Instant;
import java.util.UUID;

import com.soportehub.backend.domain.exception.BusinessRuleException;

// La empresa (tenant). Todo usuario y ticket pertenece a una.
public class Organization {

  private final UUID id;
  private final String legalName; // razon social
  private final String taxId; // RUC, unico
  private OrganizationStatus status;
  private String assignmentStrategyCode; // como reparte tickets (lo usa Ramses)
  private final Instant createdAt;

  private Organization(UUID id, String legalName, String taxId, OrganizationStatus status,
      String assignmentStrategyCode, Instant createdAt) {
    this.id = id;
    this.legalName = legalName;
    this.taxId = taxId;
    this.status = status;
    this.assignmentStrategyCode = assignmentStrategyCode;
    this.createdAt = createdAt;
  }

  // Alta nueva: nace activa y con asignacion manual
  public static Organization register(String legalName, String taxId) {
    if (legalName == null || legalName.isBlank()) {
      throw new BusinessRuleException("La razon social es obligatoria");
    }
    if (taxId == null || taxId.isBlank()) {
      throw new BusinessRuleException("El RUC es obligatorio");
    }
    return new Organization(UUID.randomUUID(), legalName.trim(), taxId.trim().toUpperCase(),
        OrganizationStatus.ACTIVE, "MANUAL", Instant.now());
  }

  // Reconstruye una empresa guardada (lo usa el adapter)
  public static Organization restore(UUID id, String legalName, String taxId, OrganizationStatus status,
      String assignmentStrategyCode, Instant createdAt) {
    return new Organization(id, legalName, taxId, status, assignmentStrategyCode, createdAt);
  }

  // Ramses la usa para cambiar la estrategia de asignacion
  public void changeAssignmentStrategy(String code) {
    if (code == null || code.isBlank()) {
      throw new BusinessRuleException("La estrategia es obligatoria");
    }
    this.assignmentStrategyCode = code;
  }

  public void suspend() {
    this.status = OrganizationStatus.SUSPENDED;
  }

  public void activate() {
    this.status = OrganizationStatus.ACTIVE;
  }

  public UUID id() {
    return id;
  }

  public String legalName() {
    return legalName;
  }

  public String taxId() {
    return taxId;
  }

  public OrganizationStatus status() {
    return status;
  }

  public String assignmentStrategyCode() {
    return assignmentStrategyCode;
  }

  public Instant createdAt() {
    return createdAt;
  }
}