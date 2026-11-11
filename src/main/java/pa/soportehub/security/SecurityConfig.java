package pa.soportehub.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfFilter; //agrego el filtro

@Configuration
@EnableMethodSecurity   // activa @PreAuthorize en los casos de uso
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService, TokenRevocationStore revocations)
            throws Exception {
        // Cookie del token CSRF con nombre PROPIO: en localhost todas las apps comparten cookies, y el nombre
        // por defecto (XSRF-TOKEN) choca con otros proyectos (por ejemplo, uno hecho con Angular).
        CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepository.setCookieName("SH-XSRF-TOKEN");
        csrfRepository.setCookiePath("/");

        http
            // Las vistas conservan CSRF (el token viaja en una cookie, sin sesión). La API con Bearer no lo necesita.
            .csrf(csrf -> csrf.csrfTokenRepository(csrfRepository)
                              .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                              .ignoringRequestMatchers("/api/**"))
            // Spring trae un /logout por defecto que se ejecuta ANTES de nuestro controlador y no revoca el token.
            // Lo apagamos para que cierre sesión AuthController (borra la cookie y revoca el jti en Redis).
            .logout(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/login",
                    "/invitation/**",
                    "/error",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico",
                    "/api/auth/login"
                ).permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(ex -> ex
                // Deja en la consola el MOTIVO de cada 403 (CSRF inválido, rol insuficiente, etc.)
                .accessDeniedHandler(SecurityConfig::deny)
                // /api devuelve 401; el navegador es enviado a la pantalla de inicio de sesión
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        request -> request.getRequestURI().startsWith("/api/"))
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
            .addFilterAfter(
                new CsrfCookieFilter(),
                CsrfFilter.class
            )
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtService, revocations),
                UsernamePasswordAuthenticationFilter.class
            );

            return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static void deny(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e)
            throws IOException {
        log.warn("403 en {} {} -> {}: {}", request.getMethod(), request.getRequestURI(),
                e.getClass().getSimpleName(), e.getMessage());
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
}
