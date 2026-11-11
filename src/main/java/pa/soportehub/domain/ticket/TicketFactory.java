package pa.soportehub.domain.ticket;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Patrón Factory: arma el ticket con estado inicial, vencimiento e historial ya calculados. */
@Component
public class TicketFactory {

    public Ticket create(UUID organizationId, UUID requesterId, UUID categoryId, String subject,
                         String description, Priority priority, int number, int slaHours) {
        Instant now = Instant.now();
        return new Ticket(UUID.randomUUID(), organizationId, number, requesterId, categoryId,
                subject.trim(), description.trim(), priority, now.plus(Duration.ofHours(slaHours)), now);
    }
}
