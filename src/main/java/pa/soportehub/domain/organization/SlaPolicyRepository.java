package pa.soportehub.domain.organization;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import pa.soportehub.domain.ticket.Priority;

public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, UUID> {

    List<SlaPolicy> findByOrganizationId(UUID organizationId);

    Optional<SlaPolicy> findByOrganizationIdAndPriority(UUID organizationId, Priority priority);
}
