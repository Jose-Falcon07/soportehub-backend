package com.soportehub.backend.application.service.listener;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.HtmlUtils;

import com.soportehub.backend.domain.event.UserInvitedEvent;
import com.soportehub.backend.domain.port.EmailSender;

/** Patrón Observer: el correo se envía DESPUÉS de confirmar la transacción. */
@Component
public class InvitationEmailListener {

  private final EmailSender emailSender;

  @Value("${app.base-url}")
  private String baseUrl;

  public InvitationEmailListener(EmailSender emailSender) {
    this.emailSender = emailSender;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(UserInvitedEvent event) {
    String link = baseUrl + "/invitation/" + event.token();
    String html = "<p>Hola " + HtmlUtils.htmlEscape(event.fullName()) + ",</p>"
        + "<p>Te invitaron a SoporteHub. Activa tu cuenta (el enlace vence en 48 horas):</p>"
        + "<p><a href=\"" + link + "\">Activar mi cuenta</a></p>";
    emailSender.send(event.email(), "Activa tu cuenta en SoporteHub", html);
  }
}