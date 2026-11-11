package pa.soportehub.security;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** Tokens revocados al cerrar sesión. Clave: sh:jwt:revoked:{jti}. */
@Component
public class TokenRevocationStore {

    private static final Logger log = LoggerFactory.getLogger(TokenRevocationStore.class);
    private final StringRedisTemplate redis;

    public TokenRevocationStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void revoke(String jti, Duration remaining) {
        if (remaining.isNegative() || remaining.isZero()) {
            return;   // el token ya venció solo
        }
        try {
            redis.opsForValue().set("sh:jwt:revoked:" + jti, "1", remaining);
        } catch (DataAccessException e) {
            log.error("Redis no responde: el token {} NO quedó revocado", jti);
        }
    }

    public boolean isRevoked(String jti) {
        try {
            return Boolean.TRUE.equals(redis.hasKey("sh:jwt:revoked:" + jti));
        } catch (DataAccessException e) {
            // DECISIÓN DEL EQUIPO (documentarla en el ADR): si Redis cae, dejamos pasar el token
            // (más disponibilidad, menos seguridad). La alternativa es devolver true y rechazar todo.
            log.warn("Redis no responde: no se puede comprobar la revocación");
            return false;
        }
    }
}
