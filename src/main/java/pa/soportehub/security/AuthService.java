package pa.soportehub.security;

import java.time.Duration;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pa.soportehub.common.InvalidCredentialsException;
import pa.soportehub.common.TooManyAttemptsException;
import pa.soportehub.domain.user.User;
import pa.soportehub.domain.user.UserRepository;
import pa.soportehub.domain.user.UserStatus;

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
        if (email == null || password == null) {
            throw new InvalidCredentialsException("Correo o contraseña incorrectos");
        }
        String normalized = email.trim().toLowerCase();
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
