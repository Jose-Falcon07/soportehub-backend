package com.soportehub.backend.application.service.command;

import com.soportehub.backend.application.dto.command.ChangeTicketStatusCommand;
import com.soportehub.backend.domain.event.TicketChangedEvent;
import com.soportehub.backend.domain.exception.BusinessRuleException;
import com.soportehub.backend.domain.exception.ForbiddenException;
import com.soportehub.backend.domain.exception.NotFoundException;
import com.soportehub.backend.domain.model.Ticket;
import com.soportehub.backend.domain.repository.TicketRepository;
import com.soportehub.backend.infrastructure.security.AuthenticatedUser;
import com.soportehub.backend.infrastructure.security.CurrentUser;
import com.soportehub.backend.infrastructure.security.TenantContext;
import com.soportehub.backend.role.entity.RoleName;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeTicketStatusCommandHandler {

    private final TicketRepository tickets;
    private final ApplicationEventPublisher events;

    public ChangeTicketStatusCommandHandler(TicketRepository tickets, ApplicationEventPublisher events) {
        this.tickets = tickets;
        this.events = events;
    }

    @PreAuthorize("hasAnyRole('SUPERVISOR','AGENT')")
    @Transactional
    public void handle(ChangeTicketStatusCommand cmd) {
        UUID org = TenantContext.require();
        AuthenticatedUser me = CurrentUser.get();
        Ticket ticket = tickets.findById(org, cmd.ticketId())
                .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));

        // Un agente solo mueve SUS tickets; el supervisor puede mover cualquiera de su empresa
        if (!me.hasRole(RoleName.SUPERVISOR) && !me.userId().equals(ticket.getAssigneeId())) {
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
