package pa.soportehub.application.query;

import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import pa.soportehub.infrastructure.redis.InboxCache;
import pa.soportehub.security.CurrentUser;
import pa.soportehub.security.TenantContext;

/** El Solicitante ve SOLO los tickets que él creó. */
@Service
public class MyTicketsQueryHandler {

    private final TicketListReader reader;
    private final InboxCache cache;

    public MyTicketsQueryHandler(TicketListReader reader, InboxCache cache) {
        this.reader = reader;
        this.cache = cache;
    }

    @PreAuthorize("hasRole('SOLICITANTE')")
    public PageResult<TicketSummaryView> handle(TicketListQuery q) {
        UUID org = TenantContext.require();
        UUID me = CurrentUser.get().userId();
        String key = "mine:" + me + ":" + q.status() + ":" + q.page() + ":" + q.size();
        return cache.getOrLoad(org, key, () -> reader.read(org, null, me, q.status(), q.page(), q.size()));
    }
}
