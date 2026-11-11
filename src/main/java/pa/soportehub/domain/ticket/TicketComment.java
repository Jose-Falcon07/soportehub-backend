package pa.soportehub.domain.ticket;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket_comments")
public class TicketComment {

    @Id
    private UUID id;
    private UUID organizationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    private UUID authorId;
    private String body;
    private boolean internalNote;
    private Instant createdAt;

    protected TicketComment() {
    }

    TicketComment(Ticket ticket, UUID authorId, String body, boolean internalNote, Instant now) {
        this.id = UUID.randomUUID();
        this.organizationId = ticket.getOrganizationId();
        this.ticket = ticket;
        this.authorId = authorId;
        this.body = body;
        this.internalNote = internalNote;
        this.createdAt = now;
    }
}
