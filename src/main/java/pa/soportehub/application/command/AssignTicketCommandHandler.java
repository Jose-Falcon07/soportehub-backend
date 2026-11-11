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
import pa.soportehub.domain.user.User;
import pa.soportehub.domain.user.UserRepository;
import pa.soportehub.domain.user.UserStatus;
import pa.soportehub.security.CurrentUser;
import pa.soportehub.security.TenantContext;

@Service
public class AssignTicketCommandHandler {

    private final TicketRepository tickets;
    private final UserRepository users;
    private final ApplicationEventPublisher events;

    public AssignTicketCommandHandler(TicketRepository tickets, UserRepository users, ApplicationEventPublisher events) {
        this.tickets = tickets;
        this.users = users;
        this.events = events;
    }

    // Dos comprobaciones distintas: el ROL (@PreAuthorize) y la EMPRESA (los findBy...Organization).
    @PreAuthorize("hasAnyRole('ADMIN_EMPRESA','SUPERVISOR')")
    @Transactional
    public void handle(AssignTicketCommand cmd) {
        UUID org = TenantContext.require();
        UUID me = CurrentUser.get().userId();

        Ticket ticket = tickets.findById(org, cmd.ticketId())
                .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));   // 404, no 403
        User agent = users.findByIdAndOrganizationId(cmd.agentId(), org)
                .orElseThrow(() -> new ForbiddenException("No puede asignar tickets a un usuario de otra empresa"));
        if (!agent.getRoles().contains(Role.AGENTE) || agent.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessRuleException("El usuario elegido no es un agente activo");
        }
        String reason = cmd.reason() == null || cmd.reason().isBlank() ? "Asignación manual" : cmd.reason();

        ticket.assignTo(agent.getId(), me, reason);
        tickets.save(ticket);
        events.publishEvent(new TicketChangedEvent(ticket.getId(), org));
    }
}
