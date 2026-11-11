package pa.soportehub.web;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pa.soportehub.security.AuthCookies;
import pa.soportehub.security.AuthService;
import pa.soportehub.security.AuthenticatedUser;

/** Misma autenticación pero para Postman/Bruno: devuelve el token en el cuerpo (y también pone la cookie). */
@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    public record LoginRequest(String email, String password) {
    }

    private final AuthService auth;
    private final AuthCookies cookies;

    public ApiAuthController(AuthService auth, AuthCookies cookies) {
        this.auth = auth;
        this.cookies = cookies;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest body, HttpServletResponse response) {
        String token = auth.login(body.email(), body.password());
        response.addHeader(HttpHeaders.SET_COOKIE, cookies.create(token));
        return Map.of("token", token);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal AuthenticatedUser user, HttpServletResponse response) {
        auth.logout(user);
        response.addHeader(HttpHeaders.SET_COOKIE, cookies.clear());
    }
}
