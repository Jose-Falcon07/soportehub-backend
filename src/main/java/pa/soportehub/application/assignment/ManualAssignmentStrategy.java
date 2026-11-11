package pa.soportehub.application.assignment;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import pa.soportehub.domain.ticket.Ticket;
import pa.soportehub.domain.user.User;

/** No elige a nadie: el supervisor asigna a mano. */
@Component
public class ManualAssignmentStrategy implements AgentAssignmentStrategy {

    @Override
    public String code() {
        return "MANUAL";
    }

    @Override
    public Optional<User> selectAgent(Ticket ticket, List<User> candidates) {
        return Optional.empty();
    }
}
