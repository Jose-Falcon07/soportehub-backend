package com.soportehub.backend.domain.event;

public record UserInvitedEvent(String email, String fullName, String token) {
}