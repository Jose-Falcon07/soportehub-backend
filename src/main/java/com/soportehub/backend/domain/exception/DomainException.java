package com.soportehub.backend.domain.exception;

// Madre de todos los errores del negocio. Abstract: siempre se lanza una hija.
public abstract class DomainException extends RuntimeException {

  // Recibe el mensaje del error y se lo pasa a RuntimeException
  protected DomainException(String message) {
    super(message);
  }
}