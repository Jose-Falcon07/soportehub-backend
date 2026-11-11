package pa.soportehub.application.assignment;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import pa.soportehub.domain.ticket.Ticket;
import pa.soportehub.domain.user.User;

/** Turno rotativo: reparte según el número del ticket (simple y sin guardar estado). */
@Component
public class RoundRobinAssignmentStrategy implements AgentAssignmentStrategy {

    @Override
    public String code() {
        return "ROUND_ROBIN";
    }

    @Override
    public Optional<User> selectAgent(Ticket ticket, List<User> candidates) {
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        List<User> ordered = candidates.stream().sorted(Comparator.comparing(User::getId)).toList();
        return Optional.of(ordered.get(ticket.getNumber() % ordered.size()));
    }
}
