package com.soportehub.backend.infrastructure.security;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookies {

    public static final String NAME = "SH_TOKEN";

    @Value("${app.cookie-secure:false}")
    private boolean secure;

    @Value("${app.jwt.expiration-minutes:60}")
    private long minutes;

    public String create(String token) {
        return ResponseCookie.from(NAME, token)
                .httpOnly(true)              // JavaScript no puede leerla
                .secure(secure)              // solo HTTPS en el entorno desplegado
                .sameSite("Strict")          // defensa principal contra CSRF
                .path("/")
                .maxAge(Duration.ofMinutes(minutes))
                .build().toString();
    }

    public String clear() {
        return ResponseCookie.from(NAME, "").httpOnly(true).secure(secure).sameSite("Strict")
                .path("/").maxAge(0).build().toString();
    }
}
