package com.soportehub.backend.invitation.dto;

public record AcceptInvitationCommand(String token, String password, String confirmation) {
}