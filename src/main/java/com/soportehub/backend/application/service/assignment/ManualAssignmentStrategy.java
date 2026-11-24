package com.soportehub.backend.application.service.assignment;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.soportehub.backend.domain.model.Ticket;
import com.soportehub.backend.user.entity.User;

/** No elige a nadie: el supervisor asigna a mano. */
@Component
public class ManualAssignmentStrategy implements AgentAssignmentStrategy {

  @Override
  public String code() {
    return "MANUAL";
  }

  @Override
  public Optional<User> selectAgent(Ticket ticket, List<User> candidates) {
    return Optional.empty();
  }
}
