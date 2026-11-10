package com.soportehub.backend.domain.exception;

// Demasiados intentos seguidos -> HTTP 429
public class TooManyRequestsException extends DomainException {

  public TooManyRequestsException(String message) {
    super(message);
  }
}