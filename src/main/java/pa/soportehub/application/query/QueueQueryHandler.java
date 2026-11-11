package pa.soportehub.application.query;

import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import pa.soportehub.infrastructure.redis.InboxCache;
import pa.soportehub.security.TenantContext;

/** Cola general de la empresa (Supervisor y Administrador de Empresa). */
@Service
public class QueueQueryHandler {

    private final TicketListReader reader;
    private final InboxCache cache;

    public QueueQueryHandler(TicketListReader reader, InboxCache cache) {
        this.reader = reader;
        this.cache = cache;
    }

    @PreAuthorize("hasAnyRole('ADMIN_EMPRESA','SUPERVISOR')")
    public PageResult<TicketSummaryView> handle(TicketListQuery q) {
        UUID org = TenantContext.require();
        String key = "queue:" + q.status() + ":" + q.page() + ":" + q.size();
        return cache.getOrLoad(org, key, () -> reader.read(org, null, null, q.status(), q.page(), q.size()));
    }
}
