package com.soportehub.backend.domain.exception;

//NO existe (o es de otra empresa) -> HTTP 404
public class NotFoundException extends DomainException {

  public NotFoundException(String message) {
    super(message);
  }
}