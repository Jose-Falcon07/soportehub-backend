package com.soportehub.backend.domain.model;

import java.util.EnumSet;
import java.util.Set;

/** Patrón State (versión sencilla): cada estado sabe a cuáles puede moverse. */
public enum TicketStatus {
    OPEN, ASSIGNED, IN_PROGRESS, WAITING_CUSTOMER, RESOLVED, CLOSED;

    public Set<TicketStatus> allowedNext() {
        return switch (this) {
            case OPEN -> EnumSet.of(ASSIGNED);
            case ASSIGNED -> EnumSet.of(IN_PROGRESS);
            case IN_PROGRESS -> EnumSet.of(WAITING_CUSTOMER, RESOLVED);
            case WAITING_CUSTOMER -> EnumSet.of(IN_PROGRESS);
            case RESOLVED -> EnumSet.of(CLOSED, IN_PROGRESS);   // IN_PROGRESS = reabrir (con ventana de 7 días)
            case CLOSED -> EnumSet.noneOf(TicketStatus.class);  // un ticket cerrado no vuelve atrás
        };
    }

    public boolean canTransitionTo(TicketStatus target) {
        return allowedNext().contains(target);
    }
}
