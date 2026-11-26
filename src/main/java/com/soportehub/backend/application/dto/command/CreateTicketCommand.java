package com.soportehub.backend.application.dto.command;

import com.soportehub.backend.domain.model.Priority;
import java.util.UUID;

public record CreateTicketCommand(UUID categoryId, String subject, String description, Priority priority) {
}
