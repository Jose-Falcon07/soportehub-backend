package pa.soportehub.application.command;

import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pa.soportehub.application.event.UserInvitedEvent;
import pa.soportehub.common.BusinessRuleException;
import pa.soportehub.domain.user.Role;
import pa.soportehub.domain.user.User;
import pa.soportehub.domain.user.UserRepository;
import pa.soportehub.infrastructure.redis.InvitationTokenStore;
import pa.soportehub.security.TenantContext;

@Service
public class InviteUserHandler {

    private final UserRepository users;
    private final InvitationTokenStore tokens;
    private final ApplicationEventPublisher events;

    public InviteUserHandler(UserRepository users, InvitationTokenStore tokens, ApplicationEventPublisher events) {
        this.users = users;
        this.tokens = tokens;
        this.events = events;
    }

    @PreAuthorize("hasRole('ADMIN_EMPRESA')")
    @Transactional
    public void handle(InviteUserCommand cmd) {
        UUID org = TenantContext.require();   // la empresa sale del token, NO del formulario
        if (cmd.role() == null || cmd.role() == Role.SUPER_ADMIN) {
            throw new BusinessRuleException("Rol no permitido");
        }
        if (cmd.email() == null || cmd.email().isBlank() || cmd.fullName() == null || cmd.fullName().isBlank()) {
            throw new BusinessRuleException("El nombre y el correo son obligatorios");
        }
        if (users.existsByEmail(cmd.email().trim().toLowerCase())) {
            throw new BusinessRuleException("Ya existe un usuario con ese correo");
        }
        User user = users.save(User.invited(org, cmd.email(), cmd.fullName(), cmd.role()));
        String token = tokens.create(user.getId());
        events.publishEvent(new UserInvitedEvent(user.getEmail(), user.getFullName(), token));
    }
}
