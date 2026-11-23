package com.soportehub.backend.infrastructure.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;

/**
 * Repositorio CSRF sin estado y sin cookie propia: el token esperado se CALCULA a partir de la sesión (jti del JWT).
 * Si no hay sesión válida no hay token esperado, por eso /login e /invitation/** quedan fuera (ver SecurityConfig).
 */
public class JwtBoundCsrfTokenRepository implements CsrfTokenRepository {

    private static final String HEADER_NAME = "X-CSRF-TOKEN";
    private static final String PARAMETER_NAME = "_csrf";

    private final JwtService jwtService;

    public JwtBoundCsrfTokenRepository(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        // Solo se usa cuando no hay sesión (páginas públicas): un valor aleatorio que nunca será válido
        return new DefaultCsrfToken(HEADER_NAME, PARAMETER_NAME, UUID.randomUUID().toString());
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        // Nada que guardar: el token se recalcula en cada petición
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        String jwt = readSessionToken(request);
        if (jwt == null) {
            return null;
        }
        try {
            String jti = jwtService.parse(jwt).getId();
            return new DefaultCsrfToken(HEADER_NAME, PARAMETER_NAME, jwtService.csrfTokenFor(jti));
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    private String readSessionToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (AuthCookies.NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
