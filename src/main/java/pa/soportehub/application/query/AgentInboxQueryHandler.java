package pa.soportehub.application.query;

import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import pa.soportehub.infrastructure.redis.InboxCache;
import pa.soportehub.security.CurrentUser;
import pa.soportehub.security.TenantContext;

/** CQRS, lado de LECTURA: no usa Ticket ni TicketRepository, devuelve la proyección directamente. */
@Service
public class AgentInboxQueryHandler {

    private final TicketListReader reader;
    private final InboxCache cache;

    public AgentInboxQueryHandler(TicketListReader reader, InboxCache cache) {
        this.reader = reader;
        this.cache = cache;
    }

    @PreAuthorize("hasAnyRole('ADMIN_EMPRESA','SUPERVISOR','AGENTE')")
    public PageResult<TicketSummaryView> handle(TicketListQuery q) {
        UUID org = TenantContext.require();
        UUID me = CurrentUser.get().userId();
        String key = "agent:" + me + ":" + q.status() + ":" + q.page() + ":" + q.size();
        return cache.getOrLoad(org, key, () -> reader.read(org, me, null, q.status(), q.page(), q.size()));
    }
}
