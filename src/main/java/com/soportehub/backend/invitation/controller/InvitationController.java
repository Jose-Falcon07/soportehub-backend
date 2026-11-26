package com.soportehub.backend.invitation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.soportehub.backend.domain.exception.BusinessRuleException;
import com.soportehub.backend.invitation.dto.AcceptInvitationCommand;
import com.soportehub.backend.invitation.service.AcceptInvitationHandler;

/**
 * Pública (el invitado aún no tiene cuenta activa): el token de Redis es la
 * credencial.
 */
@Controller
public class InvitationController {

  private final AcceptInvitationHandler accept;

  public InvitationController(AcceptInvitationHandler accept) {
    this.accept = accept;
  }

  @GetMapping("/invitation/{token}")
  public String form(@PathVariable String token, Model model) {
    model.addAttribute("token", token);
    return "accept-invitation";
  }

  @PostMapping("/invitation/{token}")
  public String submit(@PathVariable String token, @RequestParam String password,
      @RequestParam String confirmation, Model model) {
    try {
      accept.handle(new AcceptInvitationCommand(token, password, confirmation));
      return "redirect:/login?activated";
    } catch (BusinessRuleException e) {
      model.addAttribute("token", token);
      model.addAttribute("error", e.getMessage());
      return "accept-invitation";
    }
  }
}