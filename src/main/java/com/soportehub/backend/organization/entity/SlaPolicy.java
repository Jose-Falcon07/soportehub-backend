package com.soportehub.backend.organization.entity;

import java.util.UUID;

import com.soportehub.backend.domain.model.Priority;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sla_policies")
public class SlaPolicy {

  @Id
  private UUID id;
  private UUID organizationId;

  @Enumerated(EnumType.STRING)
  private Priority priority;
  private int hoursToResolve;

  protected SlaPolicy() {
  }

  public SlaPolicy(UUID organizationId, Priority priority, int hoursToResolve) {
    this.id = UUID.randomUUID();
    this.organizationId = organizationId;
    this.priority = priority;
    this.hoursToResolve = hoursToResolve;
  }

  public void changeHours(int hours) {
    this.hoursToResolve = hours;
  }

  public Priority getPriority() {
    return priority;
  }

  public int getHoursToResolve() {
    return hoursToResolve;
  }
}
