package com.soportehub.backend.application.service;

import com.soportehub.backend.domain.exception.InvalidCredentialsException;
import com.soportehub.backend.domain.exception.TooManyAttemptsException;
import com.soportehub.backend.infrastructure.security.AuthenticatedUser;
import com.soportehub.backend.infrastructure.security.JwtService;
import com.soportehub.backend.infrastructure.security.LoginAttemptLimiter;
import com.soportehub.backend.infrastructure.security.TokenRevocationStore;
import com.soportehub.backend.user.entity.User;
import com.soportehub.backend.user.entity.UserStatus;
import com.soportehub.backend.user.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final LoginAttemptLimiter limiter;
    private final TokenRevocationStore revocations;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwtService,
                       LoginAttemptLimiter limiter, TokenRevocationStore revocations) {
        this.users = users;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.limiter = limiter;
        this.revocations = revocations;
    }

    public String login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isEmpty()) {
            throw new InvalidCredentialsException( "Correo o contraseña incorrectos");
        }

        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (limiter.isBlocked(normalized)) {
            throw new TooManyAttemptsException("Demasiados intentos. Intenta de nuevo en 15 minutos");
        }
        User user = users.findByEmail(normalized).orElse(null);
        boolean ok = user != null
                && user.getStatus() == UserStatus.ACTIVE
                && user.getPasswordHash() != null
                && encoder.matches(password, user.getPasswordHash());
        if (!ok) {
            limiter.registerFailure(normalized);
            // Un solo mensaje para "no existe" y "contraseña mala": no regalamos la lista de correos válidos
            throw new InvalidCredentialsException("Correo o contraseña incorrectos");
        }
        limiter.reset(normalized);
        return jwtService.generate(user);
    }

    /** Cerrar sesión de verdad: el jti queda revocado en Redis hasta que el token venza. */
    public void logout(AuthenticatedUser user) {
        revocations.revoke(user.jti(), Duration.between(Instant.now(), user.expiresAt()));
    }
}
