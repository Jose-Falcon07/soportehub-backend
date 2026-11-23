package com.soportehub.backend.infrastructure.security;

import com.soportehub.backend.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;
    private final byte[] csrfKey;
    private final long expirationMinutes;

    public JwtService(
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.expiration-minutes:60}") long expirationMinutes) {

        if (secret == null ||
                secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException(
                "La clave JWT debe tener al menos 32 bytes"
            );
        }

        if (expirationMinutes <= 0) {
            throw new IllegalArgumentException(
                "El tiempo de expiración debe ser mayor que cero"
            );
        }

        this.key = Keys.hmacShaKeyFor(
            secret.getBytes(StandardCharsets.UTF_8)
        );

        this.expirationMinutes = expirationMinutes;

        this.csrfKey = ("csrf:" + secret)
            .getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Token CSRF derivado del jti de la sesión: HMAC(secreto, jti). No se guarda en ninguna cookie extra;
     * el servidor lo recalcula en cada petición a partir de la cookie de sesión (SH_TOKEN).
     * Un atacante no puede calcularlo porque no conoce el secreto.
     */
    public String csrfTokenFor(String jti) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(csrfKey, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(jti.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    /** El jti (identificador único) permite revocar el token al cerrar sesión. */
    public String generate(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .claim("org", user.getOrganizationId() == null ? null : user.getOrganizationId().toString())
                .claim("roles", user.getRoleNames().stream().map(Enum::name).toList())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    /** Lanza JwtException si la firma no valida o el token venció. */
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
