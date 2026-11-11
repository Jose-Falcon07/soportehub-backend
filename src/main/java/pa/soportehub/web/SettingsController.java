package pa.soportehub.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pa.soportehub.application.command.SettingsCommandHandler;
import pa.soportehub.domain.organization.CategoryRepository;
import pa.soportehub.domain.organization.OrganizationRepository;
import pa.soportehub.domain.organization.SlaPolicyRepository;
import pa.soportehub.domain.ticket.Priority;
import pa.soportehub.security.TenantContext;

@Controller
@RequestMapping("/settings")
@PreAuthorize("hasRole('ADMIN_EMPRESA')")
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
