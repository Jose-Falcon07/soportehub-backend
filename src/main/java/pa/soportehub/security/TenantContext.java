package pa.soportehub.security;

import java.util.UUID;
import pa.soportehub.common.ForbiddenException;

/**
 * Guarda la empresa de la petición actual. La llena el filtro JWT a partir del TOKEN
 * (nunca de la URL ni del cuerpo) y se limpia SIEMPRE al terminar la petición.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(UUID organizationId) {
        CURRENT.set(organizationId);
    }

    public static UUID get() {
        return CURRENT.get();
    }

    /** Para casos de uso que solo tienen sentido dentro de una empresa. */
    public static UUID require() {
        UUID id = CURRENT.get();
        if (id == null) {
            throw new ForbiddenException("Esta operación requiere pertenecer a una empresa");
        }
        return id;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
