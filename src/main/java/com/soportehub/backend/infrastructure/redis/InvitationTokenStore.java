package com.soportehub.backend.infrastructure.redis;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.soportehub.backend.domain.exception.ServiceUnavailableException;

/**
 * Tokens de invitación de un solo uso. Clave: sh:invite:{token}, vence a las 48
 * horas.
 */
@Component
public class InvitationTokenStore {

  private static final Duration TTL = Duration.ofHours(48);
  private static final String MESSAGE = "No podemos procesar invitaciones en este momento. Intenta más tarde.";
  private final StringRedisTemplate redis;
  private final SecureRandom random = new SecureRandom();

  public InvitationTokenStore(StringRedisTemplate redis) {
    this.redis = redis;
  }

  public String create(UUID userId) {
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    try {
      redis.opsForValue().set("sh:invite:" + token, userId.toString(), TTL);
    } catch (DataAccessException e) {
      throw new ServiceUnavailableException(MESSAGE);
    }
    return token;
  }

  /**
   * Devuelve el usuario y BORRA el token en el mismo paso, para que no sirva dos
   * veces.
   */
  public Optional<UUID> consume(String token) {
    try {
      String userId = redis.opsForValue().getAndDelete("sh:invite:" + token);
      return userId == null ? Optional.empty() : Optional.of(UUID.fromString(userId));
    } catch (DataAccessException e) {
      throw new ServiceUnavailableException(MESSAGE);
    }
  }
}