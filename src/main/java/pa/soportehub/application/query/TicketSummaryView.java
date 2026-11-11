package pa.soportehub.application.query;

import java.util.UUID;

/** Proyección plana, hecha a la medida de la fila de la lista. No es el agregado Ticket. */
public record TicketSummaryView(UUID id, int number, String subject, String status, String priority,
                                String category, String agentName, String dueAt, boolean overdue) {
}
