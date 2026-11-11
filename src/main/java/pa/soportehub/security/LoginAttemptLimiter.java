package pa.soportehub.security;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** Intentos fallidos de inicio de sesión. Clave: sh:login:attempts:{correo}. 5 fallos = 15 minutos bloqueado. */
@Component
public class LoginAttemptLimiter {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptLimiter.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private final StringRedisTemplate redis;

    public LoginAttemptLimiter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public boolean isBlocked(String email) {
        try {
            String value = redis.opsForValue().get(key(email));
            return value != null && Integer.parseInt(value) >= MAX_ATTEMPTS;
        } catch (DataAccessException e) {
            log.warn("Redis no responde: se permite el intento de {}", email);   // el límite es una ayuda, no el control de acceso
            return false;
        }
    }

    public void registerFailure(String email) {
        try {
            Long attempts = redis.opsForValue().increment(key(email));
            if (attempts != null && attempts == 1L) {
                redis.expire(key(email), WINDOW);
            }
        } catch (DataAccessException e) {
            log.warn("Redis no responde: no se registró el intento fallido");
        }
    }

    public void reset(String email) {
        try {
            redis.delete(key(email));
        } catch (DataAccessException e) {
            log.warn("Redis no responde al reiniciar los intentos");
        }
    }

    private String key(String email) {
        return "sh:login:attempts:" + email;
    }
}
