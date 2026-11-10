package com.soportehub.backend.domain.exception;

// Los datos llegaron, pero rompen una regla del negocio -> HTTP 422
public class BusinessRuleException extends DomainException {

  public BusinessRuleException(String message) {
    super(message);
  }
}