package pa.soportehub.infrastructure.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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

/**
 * Datos de demostración para la revisión: dos empresas con nombres bien distintos.
 * Solo se ejecuta con el perfil "demo". Contraseña de TODOS los usuarios: Demo1234!
 * (usuarios: admin@..., supervisor@..., agente@..., solicitante@... con dominio istmo.test y santaana.test)
 */
@Component
@Profile("demo")
public class DemoDataSeeder implements CommandLineRunner {

    private static final String PASSWORD = "Demo1234!";

    private final OrganizationRepository organizations;
    private final UserRepository users;
    private final CategoryRepository categories;
    private final TicketRepository tickets;
    private final TicketFactory factory;
    private final PasswordEncoder encoder;

    public DemoDataSeeder(OrganizationRepository organizations, UserRepository users, CategoryRepository categories,
                          TicketRepository tickets, TicketFactory factory, PasswordEncoder encoder) {
        this.organizations = organizations;
        this.users = users;
        this.categories = categories;
        this.tickets = tickets;
        this.factory = factory;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (organizations.count() > 0) {
            return;   // ya hay datos: no se duplica nada
        }
        users.save(User.active(null, "superadmin@soportehub.test", "Super Administrador",
                encoder.encode(PASSWORD), Role.SUPER_ADMIN));
        seedCompany("Transportes del Istmo, S.A.", "istmo.test");
        seedCompany("Clínica Santa Ana, S.A.", "santaana.test");
    }

    private void seedCompany(String name, String domain) {
        Organization org = organizations.save(new Organization(name));
        User admin = createUser(org, "admin@" + domain, "Administrador " + name, Role.ADMIN_EMPRESA);
        createUser(org, "supervisor@" + domain, "Supervisor " + name, Role.SUPERVISOR);
        User agent = createUser(org, "agente@" + domain, "Agente " + name, Role.AGENTE);
        User requester = createUser(org, "solicitante@" + domain, "Solicitante " + name, Role.SOLICITANTE);

        Category category = categories.save(new Category(org.getId(), "Soporte general"));
        categories.save(new Category(org.getId(), "Accesos y cuentas"));

        Priority[] priorities = Priority.values();
        for (int i = 1; i <= 5; i++) {
            Ticket ticket = factory.create(org.getId(), requester.getId(), category.getId(),
                    "Caso " + i + " de " + name, "Descripción de ejemplo del caso " + i,
                    priorities[i % priorities.length], tickets.nextNumber(org.getId()), 24);
            if (i % 2 == 0) {
                ticket.assignTo(agent.getId(), admin.getId(), "Asignación de demostración");
            }
            tickets.save(ticket);
        }
    }

    private User createUser(Organization org, String email, String fullName, Role role) {
        return users.save(User.active(org.getId(), email, fullName, encoder.encode(PASSWORD), role));
    }
}
