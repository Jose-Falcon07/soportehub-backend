package com.soportehub.backend.organization.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.soportehub.backend.domain.model.Priority;
import com.soportehub.backend.infrastructure.security.TenantContext;
import com.soportehub.backend.organization.repository.CategoryRepository;
import com.soportehub.backend.organization.repository.OrganizationRepository;
import com.soportehub.backend.organization.repository.SlaPolicyRepository;
import com.soportehub.backend.organization.service.SettingsCommandHandler;

@Controller
@RequestMapping("/settings")
@PreAuthorize("hasRole('COMPANY_ADMIN')")
public class SettingsController {

  private final SettingsCommandHandler settings;
  private final CategoryRepository categories;
  private final SlaPolicyRepository slaPolicies;
  private final OrganizationRepository organizations;

  public SettingsController(SettingsCommandHandler settings, CategoryRepository categories,
      SlaPolicyRepository slaPolicies, OrganizationRepository organizations) {
    this.settings = settings;
    this.categories = categories;
    this.slaPolicies = slaPolicies;
    this.organizations = organizations;
  }

  @GetMapping
  public String page(Model model) {
    var org = TenantContext.require();
    model.addAttribute("categories", categories.findByOrganizationIdOrderByName(org));
    model.addAttribute("slaPolicies", slaPolicies.findByOrganizationId(org));
    model.addAttribute("priorities", Priority.values());
    model.addAttribute("strategy", organizations.findById(org).orElseThrow().getAssignmentStrategyCode());
    return "settings";
  }

  @PostMapping("/categories")
  public String addCategory(@RequestParam String name) {
    settings.addCategory(name);
    return "redirect:/settings";
  }

  @PostMapping("/sla")
  public String saveSla(@RequestParam Priority priority, @RequestParam int hours) {
    settings.saveSla(priority, hours);
    return "redirect:/settings";
  }

  @PostMapping("/strategy")
  public String strategy(@RequestParam String code) {
    settings.changeStrategy(code);
    return "redirect:/settings";
  }
}
