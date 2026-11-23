package com.soportehub.backend.organization.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "organizations")
public class Organization {

    @Id
    private UUID id;
    private String name;
    private boolean active;
    private String assignmentStrategyCode;   // MANUAL, ROUND_ROBIN o LEAST_LOADED
    private Instant createdAt;

    protected Organization() {
    }

    public Organization(String name) {
        this.id = UUID.randomUUID();
        this.name = name.trim();
        this.active = true;
        this.assignmentStrategyCode = "MANUAL";
        this.createdAt = Instant.now();
    }

    public void changeStrategy(String code) {
        this.assignmentStrategyCode = code;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public boolean isActive() { return active; }
    public String getAssignmentStrategyCode() { return assignmentStrategyCode; }
}
