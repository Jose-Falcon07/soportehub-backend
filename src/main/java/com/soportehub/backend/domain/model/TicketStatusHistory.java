package com.soportehub.backend.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "ticket_status_history")
public class TicketStatusHistory {

    @Id
    private UUID id;
    private UUID organizationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    private TicketStatus fromStatus;

    @Enumerated(EnumType.STRING)
    private TicketStatus toStatus;

    private UUID changedBy;
    private String reason;
    private Instant changedAt;

    protected TicketStatusHistory() {
    }

    TicketStatusHistory(Ticket ticket, TicketStatus from, TicketStatus to, UUID by, String reason, Instant now) {
        this.id = UUID.randomUUID();
        this.organizationId = ticket.getOrganizationId();
        this.ticket = ticket;
        this.fromStatus = from;
        this.toStatus = to;
        this.changedBy = by;
        this.reason = reason;
        this.changedAt = now;
    }
}
