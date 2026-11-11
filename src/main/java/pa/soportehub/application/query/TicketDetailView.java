package pa.soportehub.application.query;

import java.util.List;
import java.util.UUID;

public record TicketDetailView(UUID id, int number, String subject, String description, String status,
                               String priority, String category, String requesterName, String agentName,
                               String dueAt, boolean overdue, List<CommentView> comments,
                               List<HistoryView> history, List<String> allowedNext) {

    public record CommentView(String author, String body, boolean internal, String createdAt) {}

    public record HistoryView(String user, String from, String to, String reason, String at) {}
}
