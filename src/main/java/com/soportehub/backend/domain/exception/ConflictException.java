package com.soportehub.backend.domain.exception;

// Choca con algo que ya existe o con el estado actual -> HTTP 409 
public class ConflictException extends DomainException {

  public ConflictException(String message) {
    super(message);
  }
}