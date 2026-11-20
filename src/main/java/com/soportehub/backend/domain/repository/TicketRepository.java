package com.soportehub.backend.domain.repository;

import com.soportehub.backend.domain.model.Ticket;
import java.util.Optional;
import java.util.UUID;

/** El dominio solo conoce esta interfaz; la implementación vive en infraestructura. */
public interface TicketRepository {

    Ticket save(Ticket ticket);

    /** Siempre recibe la empresa: un ticket de otra compañía simplemente "no existe". */
    Optional<Ticket> findById(UUID organizationId, UUID ticketId);

    int nextNumber(UUID organizationId);

    long countOpenByAgent(UUID organizationId, UUID agentId);
}
