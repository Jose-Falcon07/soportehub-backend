package pa.soportehub.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pa.soportehub.application.command.RegisterOrganizationCommand;
import pa.soportehub.application.command.RegisterOrganizationHandler;
import pa.soportehub.domain.organization.OrganizationRepository;

@Controller
@RequestMapping("/organizations")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class OrganizationController {

    private final OrganizationRepository organizations;
    private final RegisterOrganizationHandler register;

    public OrganizationController(OrganizationRepository organizations, RegisterOrganizationHandler register) {
        this.organizations = organizations;
        this.register = register;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("organizations", organizations.findAll());
        return "organizations";
    }

    @PostMapping
    public String create(@RequestParam String name, @RequestParam String adminName, @RequestParam String adminEmail) {
        register.handle(new RegisterOrganizationCommand(name, adminName, adminEmail));
        return "redirect:/organizations";
    }
}
