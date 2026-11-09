package com.soportehub.backend.user.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "organization_id", nullable = false)
  private UUID organizationId;

  @Column(name = "email", nullable = false, length = 180)
  private String email;

  @Column(name = "full_name", nullable = false, length = 160)
  private String fullName;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

}