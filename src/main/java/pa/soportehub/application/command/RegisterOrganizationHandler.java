package pa.soportehub.application.command;

import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pa.soportehub.application.event.UserInvitedEvent;
import pa.soportehub.common.BusinessRuleException;
import pa.soportehub.domain.organization.Organization;
import pa.soportehub.domain.organization.OrganizationRepository;
import pa.soportehub.domain.user.Role;
import pa.soportehub.domain.user.User;
import pa.soportehub.domain.user.UserRepository;
import pa.soportehub.infrastructure.redis.InvitationTokenStore;

@Service
public class RegisterOrganizationHandler {

    private final OrganizationRepository organizations;
    private final UserRepository users;
    private final InvitationTokenStore tokens;
    private final ApplicationEventPublisher events;

    public RegisterOrganizationHandler(OrganizationRepository organizations, UserRepository users,
                                       InvitationTokenStore tokens, ApplicationEventPublisher events) {
        this.organizations = organizations;
        this.users = users;
        this.tokens = tokens;
        this.events = events;
    }

    /** Crea la empresa, su primer administrador (INVITED) y la invitación, todo en UNA transacción. */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional
    public UUID handle(RegisterOrganizationCommand cmd) {
        if (isBlank(cmd.name()) || isBlank(cmd.adminName()) || isBlank(cmd.adminEmail())) {
            throw new BusinessRuleException("Todos los campos son obligatorios");
        }
        if (users.existsByEmail(cmd.adminEmail().trim().toLowerCase())) {
            throw new BusinessRuleException("Ya existe un usuario con ese correo");
        }
        Organization org = organizations.save(new Organization(cmd.name()));
        User admin = users.save(User.invited(org.getId(), cmd.adminEmail(), cmd.adminName(), Role.ADMIN_EMPRESA));
        String token = tokens.create(admin.getId());
        events.publishEvent(new UserInvitedEvent(admin.getEmail(), admin.getFullName(), token));
        return org.getId();
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
