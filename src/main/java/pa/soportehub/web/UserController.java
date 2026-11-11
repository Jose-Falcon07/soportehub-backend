package pa.soportehub.web;

import java.util.Arrays;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pa.soportehub.application.command.InviteUserCommand;
import pa.soportehub.application.command.InviteUserHandler;
import pa.soportehub.domain.user.Role;
import pa.soportehub.domain.user.UserRepository;
import pa.soportehub.security.TenantContext;

@Controller
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN_EMPRESA')")
public class UserController {

    private final UserRepository users;
    private final InviteUserHandler invite;

    public UserController(UserRepository users, InviteUserHandler invite) {
        this.users = users;
        this.invite = invite;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", users.findByOrganizationIdOrderByFullName(TenantContext.require()));
        model.addAttribute("roles", Arrays.stream(Role.values()).filter(r -> r != Role.SUPER_ADMIN).toList());
        return "users";
    }

    @PostMapping
    public String invite(@RequestParam String fullName, @RequestParam String email, @RequestParam Role role) {
        invite.handle(new InviteUserCommand(fullName, email, role));
        return "redirect:/users";
    }
}
