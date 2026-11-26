package com.soportehub.backend.application.dto.command;

import java.util.UUID;

public record AddCommentCommand(UUID ticketId, String body, boolean internal) {
}
