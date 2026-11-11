package pa.soportehub.application.command;

import java.util.UUID;

public record ReopenTicketCommand(UUID ticketId, String reason) {
}
