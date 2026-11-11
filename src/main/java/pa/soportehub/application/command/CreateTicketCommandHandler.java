package pa.soportehub.application.command;

import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pa.soportehub.application.assignment.AgentAssignmentStrategy;
import pa.soportehub.application.assignment.AssignmentStrategyResolver;
import pa.soportehub.application.event.TicketChangedEvent;
import pa.soportehub.common.BusinessRuleException;
import pa.soportehub.common.NotFoundException;
import pa.soportehub.domain.organization.CategoryRepository;
import pa.soportehub.domain.organization.Organization;
import pa.soportehub.domain.organization.OrganizationRepository;
import pa.soportehub.domain.organization.SlaPolicy;
import pa.soportehub.domain.organization.SlaPolicyRepository;
import pa.soportehub.domain.ticket.Priority;
import pa.soportehub.domain.ticket.Ticket;
import pa.soportehub.domain.ticket.TicketFactory;
import pa.soportehub.domain.ticket.TicketRepository;
import pa.soportehub.domain.user.Role;
import pa.soportehub.domain.user.User;
import pa.soportehub.domain.user.UserRepository;
import pa.soportehub.security.CurrentUser;
import pa.soportehub.security.TenantContext;

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

    @PreAuthorize("hasAnyRole('ADMIN_EMPRESA','SUPERVISOR','AGENTE','SOLICITANTE')")
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
        List<User> agents = users.findActiveByRole(org, Role.AGENTE);
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
