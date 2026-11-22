package com.soportehub.backend.infrastructure.persistence;

import com.soportehub.backend.domain.model.Ticket;
import com.soportehub.backend.domain.repository.TicketRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaTicketRepository implements TicketRepository {

    private final TicketJpaRepository jpa;

    public JpaTicketRepository(TicketJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Ticket save(Ticket ticket) {
        return jpa.save(ticket);
    }

    @Override
    public Optional<Ticket> findById(UUID organizationId, UUID ticketId) {
        return jpa.findByIdAndOrganizationId(ticketId, organizationId);
    }

    @Override
    public int nextNumber(UUID organizationId) {
        // La restricción unique (organization_id, number) evita duplicados
        // si dos personas crean un ticket exactamente al mismo tiempo.
        return jpa.maxNumber(organizationId) + 1;
    }

    @Override
    public long countOpenByAgent(UUID organizationId, UUID agentId) {
        return jpa.countOpenByAgent(organizationId, agentId);
    }
}
