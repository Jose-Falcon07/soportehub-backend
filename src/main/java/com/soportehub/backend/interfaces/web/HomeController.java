package com.soportehub.backend.interfaces.web;

import com.soportehub.backend.infrastructure.security.AuthenticatedUser;
import com.soportehub.backend.role.entity.RoleName;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Después de iniciar sesión, cada rol aterriza en su pantalla principal. */
@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home(@AuthenticationPrincipal AuthenticatedUser user) {
        if (user.hasRole(RoleName.SUPER_ADMIN)) return "redirect:/organizations";
        if (user.hasRole(RoleName.COMPANY_ADMIN) || user.hasRole(RoleName.SUPERVISOR)) return "redirect:/queue";
        if (user.hasRole(RoleName.AGENT)) return "redirect:/inbox";
        return "redirect:/my-tickets";
    }
}
