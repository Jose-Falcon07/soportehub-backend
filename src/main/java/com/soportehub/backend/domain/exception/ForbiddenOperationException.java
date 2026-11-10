package com.soportehub.backend.domain.exception;

// Tiene sesion, pero no permiso para esta accion -> HTTP 403
public class ForbiddenOperationException extends DomainException {

  public ForbiddenOperationException(String message) {
    super(message);
  }
}