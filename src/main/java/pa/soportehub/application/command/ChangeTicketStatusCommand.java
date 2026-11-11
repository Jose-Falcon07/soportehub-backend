package pa.soportehub.application.command;

import java.util.UUID;
import pa.soportehub.domain.ticket.TicketStatus;

public record ChangeTicketStatusCommand(UUID ticketId, TicketStatus target, String reason) {
}
