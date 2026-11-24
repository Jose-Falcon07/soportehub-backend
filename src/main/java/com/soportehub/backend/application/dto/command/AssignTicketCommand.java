package com.soportehub.backend.application.dto.command;

import java.util.UUID;

public record AssignTicketCommand(UUID ticketId, UUID agentId, String reason) {
}
