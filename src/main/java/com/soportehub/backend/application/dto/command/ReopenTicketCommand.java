package com.soportehub.backend.application.dto.command;

import java.util.UUID;

public record ReopenTicketCommand(UUID ticketId, String reason) {
}
