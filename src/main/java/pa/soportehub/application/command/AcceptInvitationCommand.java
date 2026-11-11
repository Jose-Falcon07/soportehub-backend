package pa.soportehub.application.command;

public record AcceptInvitationCommand(String token, String password, String confirmation) {
}
