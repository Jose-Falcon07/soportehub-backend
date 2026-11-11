package pa.soportehub.application.command;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pa.soportehub.common.BusinessRuleException;
import pa.soportehub.domain.user.User;
import pa.soportehub.domain.user.UserRepository;
import pa.soportehub.infrastructure.redis.InvitationTokenStore;

@Service
public class AcceptInvitationHandler {

    private final InvitationTokenStore tokens;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AcceptInvitationHandler(InvitationTokenStore tokens, UserRepository users, PasswordEncoder encoder) {
        this.tokens = tokens;
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public void handle(AcceptInvitationCommand cmd) {
        // Primero validamos la contraseña, para no gastar el token si el usuario se equivocó
        if (cmd.password() == null || cmd.password().length() < 8) {
            throw new BusinessRuleException("La contraseña debe tener al menos 8 caracteres");
        }
        if (!cmd.password().equals(cmd.confirmation())) {
            throw new BusinessRuleException("Las contraseñas no coinciden");
        }
        var userId = tokens.consume(cmd.token())
                .orElseThrow(() -> new BusinessRuleException("La invitación no es válida o ya venció"));
        User user = users.findById(userId)
                .orElseThrow(() -> new BusinessRuleException("La invitación no es válida o ya venció"));
        user.activate(encoder.encode(cmd.password()));
        users.save(user);
    }
}
