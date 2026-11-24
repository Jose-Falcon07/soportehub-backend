package com.soportehub.backend.application.service.assignment;

import java.util.List;
import java.util.Optional;

import com.soportehub.backend.domain.model.Ticket;
import com.soportehub.backend.user.entity.User;

/** Patrón Strategy: distintas formas de elegir al agente. */
public interface AgentAssignmentStrategy {

  /** Código que se guarda en organizations.assignment_strategy_code. */
  String code();

  Optional<User> selectAgent(Ticket ticket, List<User> candidates);
}
