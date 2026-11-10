package com.soportehub.backend.domain.exception;

// Enlace inexistente, vencido o ya usado. Mismo mensaje: no da pistas -> 422
public class InvalidInvitationException extends BusinessRuleException {

  public InvalidInvitationException() {
    super("El enlace de invitacion no es valido o ya vencio");
  }
}