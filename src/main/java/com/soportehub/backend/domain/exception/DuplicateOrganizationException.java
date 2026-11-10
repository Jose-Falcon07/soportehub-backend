package com.soportehub.backend.domain.exception;

// Ya hay una empresa con ese RUC -> 409
public class DuplicateOrganizationException extends ConflictException {

  public DuplicateOrganizationException(String taxId) {
    super("Ya existe una empresa con el RUC " + taxId);
  }
}