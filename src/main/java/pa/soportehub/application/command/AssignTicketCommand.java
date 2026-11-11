package pa.soportehub.application.command;

import java.util.UUID;

public record AssignTicketCommand(UUID ticketId, UUID agentId, String reason) {
}
