package com.soportehub.backend.role.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/** Rol guardado en la tabla roles (los 5 valores se insertan en la migración V1). */
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleName name;

    protected Role() {
    }

    public Role(RoleName name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public RoleName getName() { return name; }

    // Dos roles son iguales si tienen el mismo nombre (así funcionan bien dentro de un Set)
    @Override
    public boolean equals(Object other) {
        return other instanceof Role role && name == role.name;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return String.valueOf(name);
    }
}
