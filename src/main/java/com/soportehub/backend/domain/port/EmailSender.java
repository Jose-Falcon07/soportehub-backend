package com.soportehub.backend.domain.port;

// Puerto del dominio: al dominio no le importa si el correo sale por Resend u otro proveedor. 
public interface EmailSender {
  void send(String to, String subject, String html);
}