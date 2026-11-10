package com.soportehub.backend.domain.model.user;

// Estado de la cuenta
public enum UserStatus {
  INVITED, // invitado: sin contrasena, no puede entrar
  ACTIVE, // puede iniciar sesion
  INACTIVE // bloqueado a mano por el admin
}