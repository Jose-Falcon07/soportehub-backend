package pa.soportehub.application.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pa.soportehub.infrastructure.redis.InboxCache;

/** Patrón Observer: el comando no sabe que existe una caché; este escuchador borra lo que dejó de ser cierto. */
@Component
public class TicketCacheInvalidationHandler {

    private final InboxCache cache;

    public TicketCacheInvalidationHandler(InboxCache cache) {
        this.cache = cache;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TicketChangedEvent event) {
        cache.evictOrganization(event.organizationId());
    }
}
