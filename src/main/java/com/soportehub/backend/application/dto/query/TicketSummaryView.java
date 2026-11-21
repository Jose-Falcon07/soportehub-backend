package com.soportehub.backend.application.dto.query;

import java.util.UUID;

public record TicketSummaryView(UUID id, int number, String subject, String status, String priority, String category,
        String agentName, String dueAt, boolean overdue) {

}
