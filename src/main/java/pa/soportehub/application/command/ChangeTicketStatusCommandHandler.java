package pa.soportehub.application.command;

import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pa.soportehub.application.event.TicketChangedEvent;
import pa.soportehub.common.BusinessRuleException;
import pa.soportehub.common.ForbiddenException;
import pa.soportehub.common.NotFoundException;
import pa.soportehub.domain.ticket.Ticket;
import pa.soportehub.domain.ticket.TicketRepository;
import pa.soportehub.domain.user.Role;
import pa.soportehub.security.AuthenticatedUser;
import pa.soportehub.security.CurrentUser;
import pa.soportehub.security.TenantContext;

@Service
public class ChangeTicketStatusCommandHandler {

    private final TicketRepository tickets;
    private final ApplicationEventPublisher events;

    public ChangeTicketStatusCommandHandler(TicketRepository tickets, ApplicationEventPublisher events) {
        this.tickets = tickets;
        this.events = events;
    }

    @PreAuthorize("hasAnyRole('SUPERVISOR','AGENTE')")
    @Transactional
    public void handle(ChangeTicketStatusCommand cmd) {
        UUID org = TenantContext.require();
        AuthenticatedUser me = CurrentUser.get();
        Ticket ticket = tickets.findById(org, cmd.ticketId())
                .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));

        // Un agente solo mueve SUS tickets; el supervisor puede mover cualquiera de su empresa
        if (!me.hasRole(Role.SUPERVISOR) && !me.userId().equals(ticket.getAssigneeId())) {
            throw new ForbiddenException("Solo el agente asignado puede cambiar el estado de este ticket");
        }
        if (cmd.target() == null) {
            throw new BusinessRuleException("Debe indicar el estado destino");
        }
        switch (cmd.target()) {
            case IN_PROGRESS -> ticket.startWork(me.userId());
            case WAITING_CUSTOMER -> ticket.waitForCustomer(me.userId(), cmd.reason());
            case RESOLVED -> ticket.resolve(me.userId(), cmd.reason());
            case CLOSED -> ticket.close(me.userId());
            default -> throw new BusinessRuleException("Ese estado no se puede elegir manualmente");
        }
        tickets.save(ticket);
        events.publishEvent(new TicketChangedEvent(ticket.getId(), org));
    }
}
