package com.soportehub.backend.application.service.command;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soportehub.backend.application.dto.command.AssignTicketCommand;
import com.soportehub.backend.domain.event.TicketChangedEvent;
import com.soportehub.backend.domain.exception.BusinessRuleException;
import com.soportehub.backend.domain.exception.ForbiddenException;
import com.soportehub.backend.domain.exception.NotFoundException;
import com.soportehub.backend.domain.model.Ticket;
import com.soportehub.backend.domain.repository.TicketRepository;
import com.soportehub.backend.infrastructure.security.CurrentUser;
import com.soportehub.backend.infrastructure.security.TenantContext;
import com.soportehub.backend.role.entity.RoleName;
import com.soportehub.backend.user.entity.User;
import com.soportehub.backend.user.entity.UserStatus;
import com.soportehub.backend.user.repository.UserRepository;

@Service
public class AssignTicketCommandHandler {

    private final TicketRepository tickets;
    private final UserRepository users;
    private final ApplicationEventPublisher events;

    public AssignTicketCommandHandler(TicketRepository tickets, UserRepository users,
            ApplicationEventPublisher events) {
        this.tickets = tickets;
        this.users = users;
        this.events = events;
    }

    @PreAuthorize("hasAnyRole('COMPANY_ADMIN','SUPERVISOR')")
    @Transactional
    public void handle(AssignTicketCommand cmd) {
        UUID org = TenantContext.require();
        UUID me = CurrentUser.get().userId();

        Ticket ticket = tickets.findById(org, cmd.ticketId())
                .orElseThrow(() -> new NotFoundException("Ticket no encontrado")); // 404, no 403
        User agent = users.findByIdAndOrganizationId(cmd.agentId(), org)
                .orElseThrow(() -> new ForbiddenException("No puede asignar tickets a un usuario de otra empresa"));
        if (!agent.hasRole(RoleName.AGENT) || agent.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessRuleException("El usuario elegido no es un agente activo");
        }
        String reason = cmd.reason() == null || cmd.reason().isBlank() ? "Asignación manual" : cmd.reason();

        ticket.assignTo(agent.getId(), me, reason);
        tickets.save(ticket);
        events.publishEvent(new TicketChangedEvent(ticket.getId(), org));
    }
}
