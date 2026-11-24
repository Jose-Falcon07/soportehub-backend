package com.soportehub.backend.interfaces.rest;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.soportehub.backend.application.dto.command.AddCommentCommand;
import com.soportehub.backend.application.dto.command.AssignTicketCommand;
import com.soportehub.backend.application.service.command.AddCommentCommandHandler;
import com.soportehub.backend.application.service.command.AssignTicketCommandHandler;

@RestController
@RequestMapping("/api/v2/tickets")
public class TicketCollaborationApiController {

  public record AssignBody(UUID agentId, String reason) {
  }

  public record CommentBody(String body, boolean internal) {
  }

  private final AssignTicketCommandHandler assign;
  private final AddCommentCommandHandler addComment;

  public TicketCollaborationApiController(AssignTicketCommandHandler assign, AddCommentCommandHandler addComment) {
    this.assign = assign;
    this.addComment = addComment;
  }

  @PostMapping("/{id}/assign")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void assign(@PathVariable UUID id, @RequestBody AssignBody b) {
    assign.handle(new AssignTicketCommand(id, b.agentId(), b.reason()));
  }

  @PostMapping("/{id}/comments")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void comment(@PathVariable UUID id, @RequestBody CommentBody b) {
    addComment.handle(new AddCommentCommand(id, b.body(), b.internal()));
  }
}
