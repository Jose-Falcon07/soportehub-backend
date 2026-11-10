package com.soportehub.backend.domain.exception;

import java.util.UUID;

// El usuario no existe o es de otra empresa -> 404
public class UserNotFoundException extends NotFoundException {

  public UserNotFoundException(UUID id) {
    super("El usuario " + id + " no existe");
  }
}