package com.soportehub.backend.application.service.assignment;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.soportehub.backend.domain.model.Ticket;
import com.soportehub.backend.domain.repository.TicketRepository;
import com.soportehub.backend.user.entity.User;

/** Menor carga: elige al agente con menos tickets abiertos. */
@Component
public class LeastLoadedAssignmentStrategy implements AgentAssignmentStrategy {

  private final TicketRepository tickets;

  public LeastLoadedAssignmentStrategy(TicketRepository tickets) {
    this.tickets = tickets;
  }

  @Override
  public String code() {
    return "LEAST_LOADED";
  }

  @Override
  public Optional<User> selectAgent(Ticket ticket, List<User> candidates) {
    return candidates.stream().min(Comparator.comparingLong(
        agent -> tickets.countOpenByAgent(ticket.getOrganizationId(), agent.getId())));
  }
}
