package com.soportehub.backend.application.dto.command;

import com.soportehub.backend.domain.model.TicketStatus;
import java.util.UUID;

public record ChangeTicketStatusCommand(UUID ticketId, TicketStatus target, String reason) {
}
