package com.soportehub.backend.interfaces.web;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.soportehub.backend.application.dto.command.AddCommentCommand;
import com.soportehub.backend.application.dto.command.AssignTicketCommand;
import com.soportehub.backend.application.service.command.AddCommentCommandHandler;
import com.soportehub.backend.application.service.command.AssignTicketCommandHandler;

@Controller
@RequestMapping("/tickets")
public class TicketCollaborationController {

  private final AssignTicketCommandHandler assign;
  private final AddCommentCommandHandler addComment;

  public TicketCollaborationController(AssignTicketCommandHandler assign, AddCommentCommandHandler addComment) {
    this.assign = assign;
    this.addComment = addComment;
  }

  @PostMapping("/{id}/assign")
  public String assign(@PathVariable UUID id, @RequestParam UUID agentId, @RequestParam String reason) {
    assign.handle(new AssignTicketCommand(id, agentId, reason));
    return "redirect:/tickets/" + id;
  }

  @PostMapping("/{id}/comments")
  public String comment(@PathVariable UUID id, @RequestParam String body,
      @RequestParam(defaultValue = "false") boolean internal) {
    addComment.handle(new AddCommentCommand(id, body, internal));
    return "redirect:/tickets/" + id;
  }
}
