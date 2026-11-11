package pa.soportehub.application.command;

import java.util.UUID;

public record AddCommentCommand(UUID ticketId, String body, boolean internal) {
}
