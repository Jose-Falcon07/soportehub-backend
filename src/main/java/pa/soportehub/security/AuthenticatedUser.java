package pa.soportehub.security;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import pa.soportehub.domain.user.Role;

/** Lo que sabemos del usuario autenticado, sacado del JWT. */
public record AuthenticatedUser(UUID userId, UUID organizationId, String jti, Instant expiresAt, Set<Role> roles) {

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    /** Un Solicitante solo debe ver sus propios tickets. */
    public boolean isOnlyRequester() {
        return roles.size() == 1 && roles.contains(Role.SOLICITANTE);
    }
}
