package pa.soportehub.infrastructure.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import pa.soportehub.application.query.PageResult;
import pa.soportehub.application.query.TicketSummaryView;

/**
 * Caché de bandejas. REGLA: toda clave empieza por sh:{organizationId}: .
 * Vence a los 30 segundos: aunque falle la invalidación, el dato viejo desaparece solo.
 * Si Redis se cae, se consulta PostgreSQL directamente (más lento, mismo resultado).
 */
@Component
public class InboxCache {

    private static final Logger log = LoggerFactory.getLogger(InboxCache.class);
    private static final Duration TTL = Duration.ofSeconds(30);
    private static final TypeReference<PageResult<TicketSummaryView>> TYPE = new TypeReference<>() { };

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public InboxCache(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    public PageResult<TicketSummaryView> getOrLoad(UUID org, String suffix,
                                                   Supplier<PageResult<TicketSummaryView>> loader) {
        String key = "sh:" + org + ":inbox:" + suffix;
        try {
            String json = redis.opsForValue().get(key);
            if (json != null) {
                return mapper.readValue(json, TYPE);
            }
        } catch (Exception e) {
            log.warn("Redis no responde; se consulta PostgreSQL: {}", e.getMessage());
        }
        PageResult<TicketSummaryView> result = loader.get();
        try {
            redis.opsForValue().set(key, mapper.writeValueAsString(result), TTL);
        } catch (Exception e) {
            log.warn("No se pudo guardar en la caché: {}", e.getMessage());
        }
        return result;
    }

    /** Borra todas las bandejas cacheadas de UNA empresa (sencillo y correcto para el curso). */
    public void evictOrganization(UUID org) {
        try {
            Set<String> keys = redis.keys("sh:" + org + ":inbox:*");
            if (keys != null && !keys.isEmpty()) {
                redis.delete(keys);
            }
        } catch (Exception e) {
            log.warn("No se pudo invalidar la caché de la empresa {}: {}", org, e.getMessage());
        }
    }
}
