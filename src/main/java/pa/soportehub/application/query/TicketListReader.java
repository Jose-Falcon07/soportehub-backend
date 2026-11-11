package pa.soportehub.application.query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import pa.soportehub.common.Dates;

/** Lee directo de la base con SQL: trae exactamente las columnas que pinta la pantalla. */
@Component
public class TicketListReader {

    private static final String SELECT =
            "select t.id, t.number, t.subject, t.status, t.priority, c.name as category, "
            + "a.full_name as agent_name, t.due_at "
            + "from tickets t join categories c on c.id = t.category_id "
            + "left join users a on a.id = t.assignee_id";

    private final NamedParameterJdbcTemplate jdbc;

    public TicketListReader(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** agentId y requesterId son opcionales; la empresa (org) es SIEMPRE obligatoria. */
    public PageResult<TicketSummaryView> read(UUID org, UUID agentId, UUID requesterId, String status,
                                              int page, int size) {
        StringBuilder where = new StringBuilder(" where t.organization_id = :org");
        MapSqlParameterSource params = new MapSqlParameterSource("org", org);
        if (agentId != null) {
            where.append(" and t.assignee_id = :agent");
            params.addValue("agent", agentId);
        }
        if (requesterId != null) {
            where.append(" and t.requester_id = :requester");
            params.addValue("requester", requesterId);
        }
        if (status != null) {
            where.append(" and t.status = :status");
            params.addValue("status", status);
        }

        Long total = jdbc.queryForObject("select count(*) from tickets t" + where, params, Long.class);

        params.addValue("limit", size);
        params.addValue("offset", page * size);
        // Primero lo que no tiene responsable, luego lo que vence antes
        String sql = SELECT + where + " order by (t.assignee_id is null) desc, t.due_at asc limit :limit offset :offset";
        List<TicketSummaryView> rows = jdbc.query(sql, params, (rs, i) -> {
            Instant due = rs.getTimestamp("due_at").toInstant();
            String st = rs.getString("status");
            boolean overdue = due.isBefore(Instant.now()) && !st.equals("RESOLVED") && !st.equals("CLOSED");
            return new TicketSummaryView(rs.getObject("id", UUID.class), rs.getInt("number"),
                    rs.getString("subject"), st, rs.getString("priority"), rs.getString("category"),
                    rs.getString("agent_name"), Dates.format(due), overdue);
        });
        return new PageResult<>(rows, page, size, total == null ? 0 : total);
    }
}
