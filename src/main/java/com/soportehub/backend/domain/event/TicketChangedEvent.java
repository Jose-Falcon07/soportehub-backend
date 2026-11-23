package com.soportehub.backend.domain.event;

import java.util.UUID;

/** Evento de dominio: algo cambió en un ticket (lo escucha el Observer que invalida la caché). */
public record TicketChangedEvent(UUID ticketId, UUID organizationId) {
}
