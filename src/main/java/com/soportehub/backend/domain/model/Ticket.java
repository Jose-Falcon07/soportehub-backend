package com.soportehub.backend.domain.model;

import com.soportehub.backend.domain.exception.BusinessRuleException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Agregado Ticket. Fíjate que NO existe setStatus: el estado solo cambia por los métodos del negocio,
 * y todos pasan por moveTo(), que valida la transición y escribe el historial.
 */
@Entity
@Table(name = "tickets")
public class Ticket {

    private static final Duration REOPEN_WINDOW = Duration.ofDays(7);

    @Id
    private UUID id;
    private UUID organizationId;
    private int number;
    private UUID requesterId;
    private UUID categoryId;
    private UUID assigneeId;
    private String subject;
    private String description;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private Instant dueAt;
    private Instant resolvedAt;
    private Instant createdAt;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private List<TicketComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private List<TicketStatusHistory> history = new ArrayList<>();

    protected Ticket() {
    }

    /** Solo la fábrica (mismo paquete) puede crear tickets. */
    Ticket(UUID id, UUID organizationId, int number, UUID requesterId, UUID categoryId, String subject,
           String description, Priority priority, Instant dueAt, Instant now) {
        this.id = id;
        this.organizationId = organizationId;
        this.number = number;
        this.requesterId = requesterId;
        this.categoryId = categoryId;
        this.subject = subject;
        this.description = description;
        this.priority = priority;
        this.status = TicketStatus.OPEN;
        this.dueAt = dueAt;
        this.createdAt = now;
        history.add(new TicketStatusHistory(this, null, TicketStatus.OPEN, requesterId, "Ticket creado", now));
    }

    // ---------- reglas del negocio ----------

    public void assignTo(UUID agentId, UUID by, String reason) {
        if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED) {
            throw new BusinessRuleException("No se puede asignar un ticket resuelto o cerrado");
        }
        Instant now = Instant.now();
        this.assigneeId = agentId;
        if (status == TicketStatus.OPEN) {
            moveTo(TicketStatus.ASSIGNED, by, reason, now);
        } else {
            // Reasignación: el estado no cambia, pero queda constancia en el historial
            history.add(new TicketStatusHistory(this, status, status, by, "Reasignado: " + reason, now));
        }
    }

    public void startWork(UUID by) {
        if (status == TicketStatus.RESOLVED) {
            throw new BusinessRuleException("Para retomar un ticket resuelto use \"Reabrir\"");
        }
        moveTo(TicketStatus.IN_PROGRESS, by, "Atención en curso", Instant.now());
    }

    public void waitForCustomer(UUID by, String reason) {
        moveTo(TicketStatus.WAITING_CUSTOMER, by, reason, Instant.now());
    }

    public void resolve(UUID by, String note) {
        if (note == null || note.isBlank()) {
            throw new BusinessRuleException("Debe indicar cómo se resolvió el ticket");
        }
        Instant now = Instant.now();
        moveTo(TicketStatus.RESOLVED, by, note, now);
        this.resolvedAt = now;
    }

    public void close(UUID by) {
        moveTo(TicketStatus.CLOSED, by, "Ticket cerrado", Instant.now());
    }

    public void reopen(UUID by, String reason) {
        if (status != TicketStatus.RESOLVED) {
            throw new BusinessRuleException("Solo se puede reabrir un ticket resuelto");
        }
        Instant now = Instant.now();
        if (resolvedAt != null && resolvedAt.plus(REOPEN_WINDOW).isBefore(now)) {
            throw new BusinessRuleException("Pasaron más de 7 días desde la resolución: ya no se puede reabrir");
        }
        moveTo(TicketStatus.IN_PROGRESS, by, reason == null || reason.isBlank() ? "Reabierto" : reason, now);
        this.resolvedAt = null;
    }

    public void addComment(UUID authorId, String body, boolean internalNote) {
        if (body == null || body.isBlank()) {
            throw new BusinessRuleException("El comentario no puede estar vacío");
        }
        if (status == TicketStatus.CLOSED) {
            throw new BusinessRuleException("No se puede comentar un ticket cerrado");
        }
        comments.add(new TicketComment(this, authorId, body.trim(), internalNote, Instant.now()));
    }

    public boolean isOverdue(Instant now) {
        return dueAt != null && now.isAfter(dueAt)
                && status != TicketStatus.RESOLVED && status != TicketStatus.CLOSED;
    }

    /** ÚNICO punto por donde cambia el estado: valida la transición y escribe el historial. */
    private void moveTo(TicketStatus target, UUID by, String reason, Instant now) {
        if (!status.canTransitionTo(target)) {
            throw new BusinessRuleException("Transición no permitida: " + status + " → " + target);
        }
        TicketStatus from = this.status;
        this.status = target;
        history.add(new TicketStatusHistory(this, from, target, by, reason, now));
    }

    public UUID getId() { return id; }
    public UUID getOrganizationId() { return organizationId; }
    public int getNumber() { return number; }
    public UUID getRequesterId() { return requesterId; }
    public UUID getAssigneeId() { return assigneeId; }
    public TicketStatus getStatus() { return status; }
    public Priority getPriority() { return priority; }
    public Instant getDueAt() { return dueAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public List<TicketStatusHistory> getHistory() { return history; }
}
