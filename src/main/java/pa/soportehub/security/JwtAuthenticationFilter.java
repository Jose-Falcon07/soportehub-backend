package pa.soportehub.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import pa.soportehub.domain.user.Role;

/** Lee el token (cookie o cabecera Bearer) en cada petición y deja listo al usuario y su empresa. */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenRevocationStore revocations;

    public JwtAuthenticationFilter(JwtService jwtService, TokenRevocationStore revocations) {
        this.jwtService = jwtService;
        this.revocations = revocations;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String token = readToken(request);
            if (token != null) {
                authenticate(token);
            }
            chain.doFilter(request, response);
        } finally {
            // NO es opcional: el servidor reutiliza los hilos y si no se limpia,
            // la siguiente petición podría heredar la empresa de la anterior.
            TenantContext.clear();
        }
    }

    private void authenticate(String token) {
        try {
            Claims claims = jwtService.parse(token);
            if (revocations.isRevoked(claims.getId())) {
                return;   // sesión cerrada: se trata como si no hubiera token
            }
            String org = claims.get("org", String.class);
            UUID organizationId = org == null ? null : UUID.fromString(org);
            List<?> rawRoles = claims.get("roles", List.class);
            Set<Role> roles = rawRoles.stream().map(r -> Role.valueOf(String.valueOf(r))).collect(Collectors.toSet());

            AuthenticatedUser user = new AuthenticatedUser(UUID.fromString(claims.getSubject()), organizationId,
                    claims.getId(), claims.getExpiration().toInstant(), roles);
            List<GrantedAuthority> authorities = roles.stream()
                    .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r.name())).toList();

            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(user, null, authorities));
            TenantContext.set(organizationId);
        } catch (JwtException | IllegalArgumentException e) {
            // Token inválido o vencido: seguimos sin autenticar; SecurityConfig responderá 401 o redirigirá al login
        }
    }

    private String readToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
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
