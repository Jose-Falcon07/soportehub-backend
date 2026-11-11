package pa.soportehub.application.command;

import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pa.soportehub.application.event.TicketChangedEvent;
import pa.soportehub.common.NotFoundException;
import pa.soportehub.domain.ticket.Ticket;
import pa.soportehub.domain.ticket.TicketRepository;
import pa.soportehub.security.AuthenticatedUser;
import pa.soportehub.security.CurrentUser;
import pa.soportehub.security.TenantContext;

@Service
public class ReopenTicketCommandHandler {

    private final TicketRepository tickets;
    private final ApplicationEventPublisher events;

    public ReopenTicketCommandHandler(TicketRepository tickets, ApplicationEventPublisher events) {
        this.tickets = tickets;
        this.events = events;
    }

    @PreAuthorize("hasAnyRole('ADMIN_EMPRESA','SUPERVISOR','SOLICITANTE')")
    @Transactional
    public void handle(ReopenTicketCommand cmd) {
        UUID org = TenantContext.require();
        AuthenticatedUser me = CurrentUser.get();
        Ticket ticket = tickets.findById(org, cmd.ticketId())
                .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));
        if (me.isOnlyRequester() && !me.userId().equals(ticket.getRequesterId())) {
            throw new NotFoundException("Ticket no encontrado");
        }
        ticket.reopen(me.userId(), cmd.reason());   // la regla de los 7 días vive en el dominio
        tickets.save(ticket);
        events.publishEvent(new TicketChangedEvent(ticket.getId(), org));
    }
}
