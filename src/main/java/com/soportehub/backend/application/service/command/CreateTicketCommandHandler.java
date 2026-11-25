package com.soportehub.backend.application.service.command;

import com.soportehub.backend.application.dto.command.CreateTicketCommand;
import com.soportehub.backend.application.service.assignment.AgentAssignmentStrategy;
import com.soportehub.backend.application.service.assignment.AssignmentStrategyResolver;
import com.soportehub.backend.domain.event.TicketChangedEvent;
import com.soportehub.backend.domain.exception.BusinessRuleException;
import com.soportehub.backend.domain.exception.NotFoundException;
import com.soportehub.backend.domain.model.Priority;
import com.soportehub.backend.domain.model.Ticket;
import com.soportehub.backend.domain.model.TicketFactory;
import com.soportehub.backend.domain.repository.TicketRepository;
import com.soportehub.backend.infrastructure.security.CurrentUser;
import com.soportehub.backend.infrastructure.security.TenantContext;
import com.soportehub.backend.organization.entity.Organization;
import com.soportehub.backend.organization.entity.SlaPolicy;
import com.soportehub.backend.organization.repository.CategoryRepository;
import com.soportehub.backend.organization.repository.OrganizationRepository;
import com.soportehub.backend.organization.repository.SlaPolicyRepository;
import com.soportehub.backend.role.entity.RoleName;
import com.soportehub.backend.user.entity.User;
import com.soportehub.backend.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Patrón Command (lado de ESCRITURA de CQRS): cambia datos y devuelve solo el identificador. */
@Service
public class CreateTicketCommandHandler {

    private final TicketRepository tickets;
    private final TicketFactory factory;
    private final CategoryRepository categories;
    private final SlaPolicyRepository slaPolicies;
    private final OrganizationRepository organizations;
    private final UserRepository users;
    private final AssignmentStrategyResolver strategies;
    private final ApplicationEventPublisher events;

    public CreateTicketCommandHandler(TicketRepository tickets, TicketFactory factory, CategoryRepository categories,
                                      SlaPolicyRepository slaPolicies, OrganizationRepository organizations,
                                      UserRepository users, AssignmentStrategyResolver strategies,
                                      ApplicationEventPublisher events) {
        this.tickets = tickets;
        this.factory = factory;
        this.categories = categories;
        this.slaPolicies = slaPolicies;
        this.organizations = organizations;
        this.users = users;
        this.strategies = strategies;
        this.events = events;
    }

    @PreAuthorize("hasAnyRole('COMPANY_ADMIN','SUPERVISOR','AGENT','REQUESTER')")
    @Transactional
    public UUID handle(CreateTicketCommand cmd) {
        UUID org = TenantContext.require();
        UUID me = CurrentUser.get().userId();
        if (isBlank(cmd.subject()) || isBlank(cmd.description())) {
            throw new BusinessRuleException("El asunto y la descripción son obligatorios");
        }
        if (cmd.categoryId() == null) {
            throw new BusinessRuleException("Debe elegir una categoría");
        }
        categories.findByIdAndOrganizationId(cmd.categoryId(), org)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));

        Priority priority = cmd.priority() == null ? Priority.MEDIUM : cmd.priority();
        int hours = slaPolicies.findByOrganizationIdAndPriority(org, priority)
                .map(SlaPolicy::getHoursToResolve)
                .orElse(defaultHours(priority));

        Ticket ticket = factory.create(org, me, cmd.categoryId(), cmd.subject(), cmd.description(),
                priority, tickets.nextNumber(org), hours);
        autoAssign(ticket, org, me);
        tickets.save(ticket);
        events.publishEvent(new TicketChangedEvent(ticket.getId(), org));
        return ticket.getId();
    }

    private void autoAssign(Ticket ticket, UUID org, UUID me) {
        String code = organizations.findById(org).map(Organization::getAssignmentStrategyCode).orElse("MANUAL");
        AgentAssignmentStrategy strategy = strategies.byCode(code);
        List<User> agents = users.findActiveByRole(org, RoleName.AGENT);
        strategy.selectAgent(ticket, agents)
                .ifPresent(agent -> ticket.assignTo(agent.getId(), me, "Asignación automática"));
    }

    private int defaultHours(Priority priority) {
        return switch (priority) {
            case LOW -> 72;
            case MEDIUM -> 24;
            case HIGH -> 8;
            case URGENT -> 2;
        };
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
