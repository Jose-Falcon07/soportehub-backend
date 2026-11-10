package com.soportehub.backend.domain.exception;

// Ese correo ya existe en la misma empresa -> 409
public class EmailAlreadyRegisteredException extends ConflictException {

  public EmailAlreadyRegisteredException(String email) {
    super("El correo " + email + " ya esta registrado en esta empresa");
  }
}