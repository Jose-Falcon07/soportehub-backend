package com.soportehub.backend.interfaces.web;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.soportehub.backend.application.service.query.TicketDetailQueryHandler;
import com.soportehub.backend.domain.model.Priority;
import com.soportehub.backend.infrastructure.security.TenantContext;
import com.soportehub.backend.organization.repository.CategoryRepository;
import com.soportehub.backend.role.entity.RoleName;
import com.soportehub.backend.user.repository.UserRepository;

@Controller
@RequestMapping("/tickets")
public class TicketPageController {

  private final TicketDetailQueryHandler detail;
  private final CategoryRepository categories;
  private final UserRepository users;

  public TicketPageController(TicketDetailQueryHandler detail, CategoryRepository categories, UserRepository users) {
    this.detail = detail;
    this.categories = categories;
    this.users = users;
  }

  @GetMapping("/new")
  public String form(Model model) {
    model.addAttribute("categories", categories.findByOrganizationIdOrderByName(TenantContext.require()));
    model.addAttribute("priorities", Priority.values());
    return "new-ticket";
  }

  @GetMapping("/{id}")
  public String detail(@PathVariable UUID id, Model model) {
    model.addAttribute("d", detail.handle(id));
    model.addAttribute("agents", users.findActiveByRole(TenantContext.require(), RoleName.AGENT));
    return "ticket-detail";
  }

}
