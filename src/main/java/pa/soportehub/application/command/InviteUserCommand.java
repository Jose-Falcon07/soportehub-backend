package pa.soportehub.application.command;

import pa.soportehub.domain.user.Role;

public record InviteUserCommand(String fullName, String email, Role role) {
}
