package pa.soportehub.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pa.soportehub.domain.ticket.Ticket;

public interface TicketJpaRepository extends JpaRepository<Ticket, UUID> {

    Optional<Ticket> findByIdAndOrganizationId(UUID id, UUID organizationId);

    @Query("select coalesce(max(t.number), 0) from Ticket t where t.organizationId = :org")
    int maxNumber(@Param("org") UUID org);

    @Query("select count(t) from Ticket t where t.organizationId = :org and t.assigneeId = :agent "
            + "and t.status not in (pa.soportehub.domain.ticket.TicketStatus.RESOLVED, "
            + "pa.soportehub.domain.ticket.TicketStatus.CLOSED)")
    long countOpenByAgent(@Param("org") UUID org, @Param("agent") UUID agent);
}
