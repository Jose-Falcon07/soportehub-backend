package com.soportehub.backend.infrastructure.persistence;

import com.soportehub.backend.domain.model.Ticket;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketJpaRepository extends JpaRepository<Ticket, UUID> {

    Optional<Ticket> findByIdAndOrganizationId(UUID id, UUID organizationId);

    @Query("select coalesce(max(t.number), 0) from Ticket t where t.organizationId = :org")
    int maxNumber(@Param("org") UUID org);

    @Query("select count(t) from Ticket t where t.organizationId = :org and t.assigneeId = :agent "
            + "and t.status not in (com.soportehub.backend.domain.model.TicketStatus.RESOLVED, "
            + "com.soportehub.backend.domain.model.TicketStatus.CLOSED)")
    long countOpenByAgent(@Param("org") UUID org, @Param("agent") UUID agent);
}
