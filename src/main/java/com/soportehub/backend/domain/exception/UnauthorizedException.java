package com.soportehub.backend.domain.exception;

// No inicio sesion o sus credenciales fallaron -> HTTP 401
public class UnauthorizedException extends DomainException {

  public UnauthorizedException(String message) {
    super(message);
  }
}