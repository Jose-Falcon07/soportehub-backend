package com.soportehub.backend.domain.exception;

// Cambio de estado no permitido (ej.: activar a alguien ya activo) -> 409
public class InvalidUserStateException extends ConflictException {

  public InvalidUserStateException(String message) {
    super(message);
  }
}