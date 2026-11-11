package pa.soportehub.application.event;

public record UserInvitedEvent(String email, String fullName, String token) {
}
