package com.soportehub.backend.common.exception;

import com.soportehub.backend.domain.exception.BusinessRuleException;
import com.soportehub.backend.domain.exception.ForbiddenException;
import com.soportehub.backend.domain.exception.InvalidCredentialsException;
import com.soportehub.backend.domain.exception.NotFoundException;
import com.soportehub.backend.domain.exception.ServiceUnavailableException;
import com.soportehub.backend.domain.exception.TooManyAttemptsException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/** Convierte las excepciones del negocio en códigos HTTP claros (JSON para /api, página para el navegador). */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({NotFoundException.class, ForbiddenException.class, BusinessRuleException.class,
            InvalidCredentialsException.class, TooManyAttemptsException.class, ServiceUnavailableException.class})
    public Object handle(RuntimeException ex, HttpServletRequest request) {
        HttpStatus status = statusOf(ex);
        log.warn("{} {} -> {} {}: {}", request.getMethod(), request.getRequestURI(), status.value(),
                ex.getClass().getSimpleName(), ex.getMessage());
        if (request.getRequestURI().startsWith("/api/")) {
            return ResponseEntity.status(status).body(Map.of("status", status.value(), "message", ex.getMessage()));
        }
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(status);
        mav.addObject("status", status.value());
        mav.addObject("message", ex.getMessage());
        return mav;
    }

    private HttpStatus statusOf(Exception ex) {
        if (ex instanceof NotFoundException) return HttpStatus.NOT_FOUND;                     // 404
        if (ex instanceof ForbiddenException) return HttpStatus.FORBIDDEN;                    // 403
        if (ex instanceof InvalidCredentialsException) return HttpStatus.UNAUTHORIZED;        // 401
        if (ex instanceof TooManyAttemptsException) return HttpStatus.TOO_MANY_REQUESTS;      // 429
        if (ex instanceof ServiceUnavailableException) return HttpStatus.SERVICE_UNAVAILABLE; // 503
        return HttpStatus.UNPROCESSABLE_ENTITY;                                               // 422 (reglas de negocio)
    }
}
