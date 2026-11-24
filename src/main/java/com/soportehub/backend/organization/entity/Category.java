package com.soportehub.backend.organization.entity;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {

  @Id
  private UUID id;
  private UUID organizationId;
  private String name;

  protected Category() {
  }

  public Category(UUID organizationId, String name) {
    this.id = UUID.randomUUID();
    this.organizationId = organizationId;
    this.name = name.trim();
  }

  public UUID getId() {
    return id;
  }

  public UUID getOrganizationId() {
    return organizationId;
  }

  public String getName() {
    return name;
  }
}