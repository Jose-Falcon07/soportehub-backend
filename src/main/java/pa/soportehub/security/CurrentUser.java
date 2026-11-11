package pa.soportehub.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pa.soportehub.common.ForbiddenException;

/** Atajo para obtener al usuario autenticado desde los casos de uso. */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static AuthenticatedUser get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        throw new ForbiddenException("No hay un usuario autenticado");
    }
}
