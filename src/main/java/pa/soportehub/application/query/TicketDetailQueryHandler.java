package pa.soportehub.application.query;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import pa.soportehub.common.Dates;
import pa.soportehub.common.NotFoundException;
import pa.soportehub.domain.ticket.TicketStatus;
import pa.soportehub.security.AuthenticatedUser;
import pa.soportehub.security.CurrentUser;
import pa.soportehub.security.TenantContext;

@Service
public class TicketDetailQueryHandler {

    private record Head(UUID id, int number, String subject, String description, String status, String priority,
                        String category, String requester, String agent, Instant due) {}

    private final NamedParameterJdbcTemplate jdbc;

    public TicketDetailQueryHandler(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PreAuthorize("isAuthenticated()")
    public TicketDetailView handle(UUID ticketId) {
        UUID org = TenantContext.require();
        AuthenticatedUser me = CurrentUser.get();
        MapSqlParameterSource p = new MapSqlParameterSource("id", ticketId).addValue("org", org);

        String sql = "select t.id, t.number, t.subject, t.description, t.status, t.priority, c.name as category, "
                + "r.full_name as requester, a.full_name as agent, t.due_at "
                + "from tickets t join categories c on c.id = t.category_id "
                + "join users r on r.id = t.requester_id left join users a on a.id = t.assignee_id "
                + "where t.id = :id and t.organization_id = :org";   // la empresa SIEMPRE va en la consulta
        if (me.isOnlyRequester()) {
            sql += " and t.requester_id = :me";                       // un solicitante solo ve lo suyo
            p.addValue("me", me.userId());
        }
        List<Head> found = jdbc.query(sql, p, (rs, i) -> new Head(rs.getObject("id", UUID.class), rs.getInt("number"),
                rs.getString("subject"), rs.getString("description"), rs.getString("status"),
                rs.getString("priority"), rs.getString("category"), rs.getString("requester"),
                rs.getString("agent"), rs.getTimestamp("due_at").toInstant()));
        if (found.isEmpty()) {
            // Mismo mensaje si no existe o si es de otra empresa: 404, nunca 403
            throw new NotFoundException("Ticket no encontrado");
        }
        Head h = found.get(0);

        String commentsSql = "select u.full_name as author, c.body, c.internal_note, c.created_at "
                + "from ticket_comments c join users u on u.id = c.author_id where c.ticket_id = :id"
                + (me.isOnlyRequester() ? " and c.internal_note = false" : "") + " order by c.created_at";
        List<TicketDetailView.CommentView> comments = jdbc.query(commentsSql, new MapSqlParameterSource("id", ticketId),
                (rs, i) -> new TicketDetailView.CommentView(rs.getString("author"), rs.getString("body"),
                        rs.getBoolean("internal_note"), Dates.format(rs.getTimestamp("created_at").toInstant())));

        String historySql = "select u.full_name as who, s.from_status, s.to_status, s.reason, s.changed_at "
                + "from ticket_status_history s join users u on u.id = s.changed_by "
                + "where s.ticket_id = :id order by s.changed_at";
        List<TicketDetailView.HistoryView> history = jdbc.query(historySql, new MapSqlParameterSource("id", ticketId),
                (rs, i) -> new TicketDetailView.HistoryView(rs.getString("who"), rs.getString("from_status"),
                        rs.getString("to_status"), rs.getString("reason"),
                        Dates.format(rs.getTimestamp("changed_at").toInstant())));

        boolean overdue = h.due().isBefore(Instant.now()) && !h.status().equals("RESOLVED") && !h.status().equals("CLOSED");
        return new TicketDetailView(h.id(), h.number(), h.subject(), h.description(), h.status(), h.priority(),
                h.category(), h.requester(), h.agent(), Dates.format(h.due()), overdue, comments, history,
                allowedTargets(h.status()));
    }

    /** Estados que el formulario "Cambiar estado" puede ofrecer (asignar y reabrir tienen su propio botón). */
    private List<String> allowedTargets(String status) {
        List<String> result = new ArrayList<>();
        for (TicketStatus next : TicketStatus.valueOf(status).allowedNext()) {
            boolean viaOtherForm = next == TicketStatus.ASSIGNED
                    || (TicketStatus.valueOf(status) == TicketStatus.RESOLVED && next == TicketStatus.IN_PROGRESS);
            if (!viaOtherForm) {
                result.add(next.name());
            }
        }
        return result;
    }
}
