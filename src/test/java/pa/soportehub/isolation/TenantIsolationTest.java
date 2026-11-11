package pa.soportehub.isolation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pa.soportehub.domain.organization.Category;
import pa.soportehub.domain.organization.CategoryRepository;
import pa.soportehub.domain.organization.Organization;
import pa.soportehub.domain.organization.OrganizationRepository;
import pa.soportehub.domain.ticket.Priority;
import pa.soportehub.domain.ticket.Ticket;
import pa.soportehub.domain.ticket.TicketFactory;
import pa.soportehub.domain.ticket.TicketRepository;
import pa.soportehub.domain.user.Role;
import pa.soportehub.domain.user.User;
import pa.soportehub.domain.user.UserRepository;
import pa.soportehub.security.JwtService;

/**
 * Requiere Docker. Verifica lo MÁS importante de la fase: una empresa no puede ver datos de otra,
 * ni por detalle ni por lista, y responde 404 (no 403) para no revelar que el ticket existe.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "DB_USER=test", "DB_PASSWORD=test",
        "app.jwt.secret=una-clave-solo-para-pruebas-de-al-menos-32-caracteres"})
class TenantIsolationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @Autowired MockMvc mvc;
    @Autowired JwtService jwt;
    @Autowired OrganizationRepository organizations;
    @Autowired UserRepository users;
    @Autowired CategoryRepository categories;
    @Autowired TicketRepository tickets;
    @Autowired TicketFactory factory;

    private UUID ticketOfCompanyA;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void seed() {
        Organization a = organizations.save(new Organization("Empresa A " + UUID.randomUUID()));
        Organization b = organizations.save(new Organization("Empresa B " + UUID.randomUUID()));

        User supervisorA = users.save(User.active(a.getId(), uniqueEmail(), "Sup A", "x", Role.SUPERVISOR));
        User supervisorB = users.save(User.active(b.getId(), uniqueEmail(), "Sup B", "x", Role.SUPERVISOR));
        Category categoryA = categories.save(new Category(a.getId(), "General"));

        Ticket ticket = factory.create(a.getId(), supervisorA.getId(), categoryA.getId(),
                "Ticket de A", "Detalle", Priority.MEDIUM, tickets.nextNumber(a.getId()), 24);
        tickets.save(ticket);

        ticketOfCompanyA = ticket.getId();
        tokenA = jwt.generate(supervisorA);
        tokenB = jwt.generate(supervisorB);
    }

    @Test
    void laEmpresaDueñaVeSuTicket() throws Exception {
        mvc.perform(get("/api/tickets/{id}", ticketOfCompanyA).header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());
    }

    @Test
    void otraEmpresaRecibe404YNoSabeQueElTicketExiste() throws Exception {
        mvc.perform(get("/api/tickets/{id}", ticketOfCompanyA).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    void laColaDeOtraEmpresaNoContieneTicketsAjenos() throws Exception {
        mvc.perform(get("/api/queue").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void sinTokenSeRecibe401() throws Exception {
        mvc.perform(get("/api/queue")).andExpect(status().isUnauthorized());
    }

    private String uniqueEmail() {
        return UUID.randomUUID() + "@prueba.test";
    }
}
