package com.soportehub.backend.infrastructure.config;

import com.soportehub.backend.infrastructure.security.JwtAuthenticationFilter;
import com.soportehub.backend.infrastructure.security.JwtBoundCsrfTokenRepository;
import com.soportehub.backend.infrastructure.security.JwtService;
import com.soportehub.backend.infrastructure.security.TokenRevocationStore;
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
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableMethodSecurity   // activa @PreAuthorize en los casos de uso
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService, TokenRevocationStore revocations)
            throws Exception {
        // Token CSRF ligado a la sesión (jti del JWT): no depende de ninguna cookie adicional
        JwtBoundCsrfTokenRepository csrfRepository = new JwtBoundCsrfTokenRepository(jwtService);

        http
            // Todos los formularios de la sesión (crear ticket, comentar, crear empresa, invitar, configurar...)
            // exigen el token CSRF. Quedan SIN token solo:
            //  - /api/**: usa Bearer, no cookie.
            //  - /login e /invitation/**: aún no hay sesión de la que derivar el token (el token de invitación ya es
            //    un secreto de un solo uso, y el login no expone datos).
            //  - /logout: forzar un cierre de sesión es de riesgo mínimo; SH_TOKEN usa SameSite=Strict.
            .csrf(csrf -> csrf.csrfTokenRepository(csrfRepository)
                              .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                              .ignoringRequestMatchers("/api/**", "/login", "/invitation/**", "/logout"))
            // Spring trae un /logout por defecto que se ejecuta ANTES de nuestro controlador y no revoca el token.
            // Lo apagamos para que cierre sesión AuthController (borra la cookie y revoca el jti en Redis).
            .logout(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/invitation/**", "/error", "/css/**", "/api/auth/login",
                        // Rutas de salud (HealthController y Actuator): ajusten si su HealthController usa otra
                        "/health", "/api/health", "/api/v1/health", "/actuator/health/**").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(ex -> ex
                // Deja en la consola el MOTIVO de cada 403 (CSRF inválido, rol insuficiente, etc.)
                .accessDeniedHandler(SecurityConfig::deny)
                // /api devuelve 401; el navegador es enviado a la pantalla de inicio de sesión
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        request -> request.getRequestURI().startsWith("/api/"))
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
            .addFilterBefore(new JwtAuthenticationFilter(jwtService, revocations),
                    UsernamePasswordAuthenticationFilter.class);
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
