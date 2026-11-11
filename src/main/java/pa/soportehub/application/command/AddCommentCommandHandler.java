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
public class AddCommentCommandHandler {

    private final TicketRepository tickets;
    private final ApplicationEventPublisher events;

    public AddCommentCommandHandler(TicketRepository tickets, ApplicationEventPublisher events) {
        this.tickets = tickets;
        this.events = events;
    }

    @PreAuthorize("hasAnyRole('SUPERVISOR','AGENTE','SOLICITANTE')")
    @Transactional
    public void handle(AddCommentCommand cmd) {
        UUID org = TenantContext.require();
        AuthenticatedUser me = CurrentUser.get();
        Ticket ticket = tickets.findById(org, cmd.ticketId())
                .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));

        // Un solicitante solo participa en SUS tickets y nunca escribe notas internas
        if (me.isOnlyRequester() && !me.userId().equals(ticket.getRequesterId())) {
            throw new NotFoundException("Ticket no encontrado");
        }
        boolean internal = cmd.internal() && !me.isOnlyRequester();

        ticket.addComment(me.userId(), cmd.body(), internal);
        tickets.save(ticket);
        events.publishEvent(new TicketChangedEvent(ticket.getId(), org));
    }
}
