package pa.soportehub.domain.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pa.soportehub.common.BusinessRuleException;

class TicketTest {

    private final TicketFactory factory = new TicketFactory();
    private final UUID agent = UUID.randomUUID();
    private final UUID supervisor = UUID.randomUUID();

    private Ticket newTicket() {
        return factory.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "Asunto", "Detalle", Priority.MEDIUM, 1, 24);
    }

    @Test
    void unTicketNuevoEstaAbiertoYTieneHistorial() {
        Ticket t = newTicket();
        assertEquals(TicketStatus.OPEN, t.getStatus());
        assertEquals(1, t.getHistory().size());
    }

    @Test
    void noSePuedePasarDeOpenAClosed() {
        Ticket t = newTicket();
        assertThrows(BusinessRuleException.class, () -> t.close(supervisor));
    }

    @Test
    void asignarMueveAAssignedYEscribeHistorial() {
        Ticket t = newTicket();
        t.assignTo(agent, supervisor, "Prueba");
        assertEquals(TicketStatus.ASSIGNED, t.getStatus());
        assertEquals(agent, t.getAssigneeId());
        assertEquals(2, t.getHistory().size());
    }

    @Test
    void cicloCompletoYUnCerradoNoSeReasigna() {
        Ticket t = newTicket();
        t.assignTo(agent, supervisor, "Prueba");
        t.startWork(agent);
        t.resolve(agent, "Se reinició el servicio");
        t.close(supervisor);
        assertEquals(TicketStatus.CLOSED, t.getStatus());
        assertThrows(BusinessRuleException.class, () -> t.assignTo(agent, supervisor, "Otra vez"));
    }

    @Test
    void sePuedeReabrirUnResueltoDentroDeLaVentana() {
        Ticket t = newTicket();
        t.assignTo(agent, supervisor, "Prueba");
        t.startWork(agent);
        t.resolve(agent, "Listo");
        t.reopen(supervisor, "Sigue fallando");
        assertEquals(TicketStatus.IN_PROGRESS, t.getStatus());
    }

    @Test
    void soloSeReabreUnTicketResuelto() {
        Ticket t = newTicket();
        assertThrows(BusinessRuleException.class, () -> t.reopen(supervisor, "x"));
    }

    @Test
    void resolverExigeUnaNota() {
        Ticket t = newTicket();
        t.assignTo(agent, supervisor, "Prueba");
        t.startWork(agent);
        assertThrows(BusinessRuleException.class, () -> t.resolve(agent, " "));
    }

    @Test
    void unTicketVencidoSeDetecta() {
        Ticket t = newTicket();
        assertTrue(t.isOverdue(Instant.now().plusSeconds(3600L * 48)));
    }
}
