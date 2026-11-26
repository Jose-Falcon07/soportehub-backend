package com.soportehub.backend.application.service.command;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soportehub.backend.application.dto.command.AddCommentCommand;
import com.soportehub.backend.domain.event.TicketChangedEvent;
import com.soportehub.backend.domain.exception.NotFoundException;
import com.soportehub.backend.domain.model.Ticket;
import com.soportehub.backend.domain.repository.TicketRepository;
import com.soportehub.backend.infrastructure.security.AuthenticatedUser;
import com.soportehub.backend.infrastructure.security.CurrentUser;
import com.soportehub.backend.infrastructure.security.TenantContext;

@Service
public class AddCommentCommandHandler {

  private final TicketRepository tickets;
  private final ApplicationEventPublisher events;

  public AddCommentCommandHandler(TicketRepository tickets, ApplicationEventPublisher events) {
    this.tickets = tickets;
    this.events = events;
  }

  @PreAuthorize("hasAnyRole('SUPERVISOR','AGENT','REQUESTER')")
  @Transactional
  public void handle(AddCommentCommand cmd) {
    UUID org = TenantContext.require();
    AuthenticatedUser me = CurrentUser.get();
    Ticket ticket = tickets.findById(org, cmd.ticketId())
        .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));

    // Un solicitante solo participa en SUS tickets y nunca escribe notas internas
    if (me.isOnlyRequester() && !me.userId().equals(ticket.getRequesterId())) {
      throw new NotFoundException("Ticket no encontrado");
    }
    boolean internal = cmd.internal() && !me.isOnlyRequester();

    ticket.addComment(me.userId(), cmd.body(), internal);
    tickets.save(ticket);
    events.publishEvent(new TicketChangedEvent(ticket.getId(), org));
  }
}
