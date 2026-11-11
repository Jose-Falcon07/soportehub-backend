package pa.soportehub.web;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pa.soportehub.application.command.AddCommentCommand;
import pa.soportehub.application.command.AddCommentCommandHandler;
import pa.soportehub.application.command.AssignTicketCommand;
import pa.soportehub.application.command.AssignTicketCommandHandler;
import pa.soportehub.application.command.ChangeTicketStatusCommand;
import pa.soportehub.application.command.ChangeTicketStatusCommandHandler;
import pa.soportehub.application.command.CreateTicketCommand;
import pa.soportehub.application.command.CreateTicketCommandHandler;
import pa.soportehub.application.command.ReopenTicketCommand;
import pa.soportehub.application.command.ReopenTicketCommandHandler;
import pa.soportehub.domain.ticket.Priority;
import pa.soportehub.domain.ticket.TicketStatus;

/** Acciones de ESCRITURA desde las pantallas. Las lecturas están en TicketPageController (P4). */
@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final CreateTicketCommandHandler create;
    private final AssignTicketCommandHandler assign;
    private final ChangeTicketStatusCommandHandler changeStatus;
    private final AddCommentCommandHandler addComment;
    private final ReopenTicketCommandHandler reopen;

    public TicketController(CreateTicketCommandHandler create, AssignTicketCommandHandler assign,
                            ChangeTicketStatusCommandHandler changeStatus, AddCommentCommandHandler addComment,
                            ReopenTicketCommandHandler reopen) {
        this.create = create;
        this.assign = assign;
        this.changeStatus = changeStatus;
        this.addComment = addComment;
        this.reopen = reopen;
    }

    @PostMapping
    public String create(@RequestParam UUID categoryId, @RequestParam String subject,
                         @RequestParam String description, @RequestParam Priority priority) {
        UUID id = create.handle(new CreateTicketCommand(categoryId, subject, description, priority));
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/assign")
    public String assign(@PathVariable UUID id, @RequestParam UUID agentId, @RequestParam String reason) {
        assign.handle(new AssignTicketCommand(id, agentId, reason));
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/status")
    public String status(@PathVariable UUID id, @RequestParam TicketStatus target,
                         @RequestParam(required = false) String reason) {
        changeStatus.handle(new ChangeTicketStatusCommand(id, target, reason));
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/comments")
    public String comment(@PathVariable UUID id, @RequestParam String body,
                          @RequestParam(defaultValue = "false") boolean internal) {
        addComment.handle(new AddCommentCommand(id, body, internal));
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/reopen")
    public String reopen(@PathVariable UUID id, @RequestParam(required = false) String reason) {
        reopen.handle(new ReopenTicketCommand(id, reason));
        return "redirect:/tickets/" + id;
    }
}
