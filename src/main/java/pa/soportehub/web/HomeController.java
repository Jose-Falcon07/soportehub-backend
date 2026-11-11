package pa.soportehub.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import pa.soportehub.domain.user.Role;
import pa.soportehub.security.AuthenticatedUser;

/** Después de iniciar sesión, cada rol aterriza en su pantalla principal. */
@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home(@AuthenticationPrincipal AuthenticatedUser user) {
        if (user.hasRole(Role.SUPER_ADMIN)) return "redirect:/organizations";
        if (user.hasRole(Role.ADMIN_EMPRESA) || user.hasRole(Role.SUPERVISOR)) return "redirect:/queue";
        if (user.hasRole(Role.AGENTE)) return "redirect:/inbox";
        return "redirect:/my-tickets";
    }
}
