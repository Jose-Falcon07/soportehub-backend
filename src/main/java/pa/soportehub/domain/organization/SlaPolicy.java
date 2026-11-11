package pa.soportehub.domain.organization;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import pa.soportehub.domain.ticket.Priority;

@Entity
@Table(name = "sla_policies")
public class SlaPolicy {

    @Id
    private UUID id;
    private UUID organizationId;

    @Enumerated(EnumType.STRING)
    private Priority priority;
    private int hoursToResolve;

    protected SlaPolicy() {
    }

    public SlaPolicy(UUID organizationId, Priority priority, int hoursToResolve) {
        this.id = UUID.randomUUID();
        this.organizationId = organizationId;
        this.priority = priority;
        this.hoursToResolve = hoursToResolve;
    }

    public void changeHours(int hours) {
        this.hoursToResolve = hours;
    }

    public Priority getPriority() { return priority; }
    public int getHoursToResolve() { return hoursToResolve; }
}
