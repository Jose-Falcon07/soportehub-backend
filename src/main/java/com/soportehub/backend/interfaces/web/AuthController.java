package com.soportehub.backend.interfaces.web;

import com.soportehub.backend.application.service.AuthService;
import com.soportehub.backend.domain.exception.InvalidCredentialsException;
import com.soportehub.backend.domain.exception.TooManyAttemptsException;
import com.soportehub.backend.infrastructure.security.AuthCookies;
import com.soportehub.backend.infrastructure.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final AuthService auth;
    private final AuthCookies cookies;

    public AuthController(AuthService auth, AuthCookies cookies) {
        this.auth = auth;
        this.cookies = cookies;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        HttpServletResponse response, Model model) {
        try {
            String token = auth.login(email, password);
            response.addHeader(HttpHeaders.SET_COOKIE, cookies.create(token));
            return "redirect:/home";
        } catch (InvalidCredentialsException | TooManyAttemptsException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @PostMapping("/logout")
    public String logout(@AuthenticationPrincipal AuthenticatedUser user, HttpServletResponse response) {
        if (user != null) {
            auth.logout(user);   // revoca el token en Redis
        }
        response.addHeader(HttpHeaders.SET_COOKIE, cookies.clear());
        return "redirect:/login";
    }
}
