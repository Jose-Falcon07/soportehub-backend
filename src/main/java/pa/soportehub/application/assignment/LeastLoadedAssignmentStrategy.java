package pa.soportehub.application.assignment;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import pa.soportehub.domain.ticket.Ticket;
import pa.soportehub.domain.ticket.TicketRepository;
import pa.soportehub.domain.user.User;

/** Menor carga: elige al agente con menos tickets abiertos. */
@Component
public class LeastLoadedAssignmentStrategy implements AgentAssignmentStrategy {

    private final TicketRepository tickets;

    public LeastLoadedAssignmentStrategy(TicketRepository tickets) {
        this.tickets = tickets;
    }

    @Override
    public String code() {
        return "LEAST_LOADED";
    }

    @Override
    public Optional<User> selectAgent(Ticket ticket, List<User> candidates) {
        return candidates.stream().min(Comparator.comparingLong(
                agent -> tickets.countOpenByAgent(ticket.getOrganizationId(), agent.getId())));
    }
}
