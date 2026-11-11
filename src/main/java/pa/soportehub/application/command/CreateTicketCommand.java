package pa.soportehub.application.command;

import java.util.UUID;
import pa.soportehub.domain.ticket.Priority;

public record CreateTicketCommand(UUID categoryId, String subject, String description, Priority priority) {
}
