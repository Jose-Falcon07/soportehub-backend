package pa.soportehub.application.query;

import pa.soportehub.common.BusinessRuleException;
import pa.soportehub.domain.ticket.TicketStatus;

/** Filtros de la lista. El constructor acota el tamaño de página: es una medida de seguridad. */
public record TicketListQuery(String status, int page, int size) {

    public TicketListQuery {
        status = (status == null || status.isBlank()) ? null : status.trim();
        if (status != null) {
            try {
                TicketStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleException("Estado no válido");
            }
        }
        page = Math.max(page, 0);
        size = Math.min(Math.max(size, 1), 50);   // nunca más de 50 filas por petición
    }
}
