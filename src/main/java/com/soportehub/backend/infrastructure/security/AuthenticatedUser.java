package com.soportehub.backend.infrastructure.security;

import com.soportehub.backend.role.entity.RoleName;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/** Lo que sabemos del usuario autenticado, sacado del JWT. */
public record AuthenticatedUser(UUID userId, UUID organizationId, String jti, Instant expiresAt, Set<RoleName> roles) {

    public boolean hasRole(RoleName role) {
        return roles.contains(role);
    }

    /** Un Solicitante solo debe ver sus propios tickets. */
    public boolean isOnlyRequester() {
        return roles.size() == 1 && roles.contains(RoleName.REQUESTER);
    }
}
