package com.soportehub.backend.domain.exception;

import java.util.UUID;

// La empresa buscada no existe -> 404
public class OrganizationNotFoundException extends NotFoundException {

  public OrganizationNotFoundException(UUID id) {
    super("La empresa " + id + " no existe");
  }
}