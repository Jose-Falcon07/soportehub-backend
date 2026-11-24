package com.soportehub.backend.application.service.assignment;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.soportehub.backend.domain.model.Ticket;
import com.soportehub.backend.user.entity.User;

/**
 * Turno rotativo: reparte según el número del ticket (simple y sin guardar
 * estado).
 */
@Component
public class RoundRobinAssignmentStrategy implements AgentAssignmentStrategy {

  @Override
  public String code() {
    return "ROUND_ROBIN";
  }

  @Override
  public Optional<User> selectAgent(Ticket ticket, List<User> candidates) {
    if (candidates.isEmpty()) {
      return Optional.empty();
    }
    List<User> ordered = candidates.stream().sorted(Comparator.comparing(User::getId)).toList();
    return Optional.of(ordered.get(ticket.getNumber() % ordered.size()));
  }
}
