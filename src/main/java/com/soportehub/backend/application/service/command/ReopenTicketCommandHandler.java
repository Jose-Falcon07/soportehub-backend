package com.soportehub.backend.application.service.command;

import com.soportehub.backend.application.dto.command.ReopenTicketCommand;
import com.soportehub.backend.domain.event.TicketChangedEvent;
import com.soportehub.backend.domain.exception.NotFoundException;
import com.soportehub.backend.domain.model.Ticket;
import com.soportehub.backend.domain.repository.TicketRepository;
import com.soportehub.backend.infrastructure.security.AuthenticatedUser;
import com.soportehub.backend.infrastructure.security.CurrentUser;
import com.soportehub.backend.infrastructure.security.TenantContext;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReopenTicketCommandHandler {

    private final TicketRepository tickets;
    private final ApplicationEventPublisher events;

    public ReopenTicketCommandHandler(TicketRepository tickets, ApplicationEventPublisher events) {
        this.tickets = tickets;
        this.events = events;
    }

    @PreAuthorize("hasAnyRole('COMPANY_ADMIN','SUPERVISOR','REQUESTER')")
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
