package com.soportehub.backend.domain.exception;

//INtento de mezclar datos de dos empresas --> HTTP 403
public class CrossTenantOperationException extends ForbiddenOperationException {

  public CrossTenantOperationException(String message) {
    super(message);
  }
}