package pa.soportehub.application.assignment;

import java.util.List;
import java.util.Optional;
import pa.soportehub.domain.ticket.Ticket;
import pa.soportehub.domain.user.User;

/** Patrón Strategy: distintas formas de elegir al agente. */
public interface AgentAssignmentStrategy {

    /** Código que se guarda en organizations.assignment_strategy_code. */
    String code();

    Optional<User> selectAgent(Ticket ticket, List<User> candidates);
}
