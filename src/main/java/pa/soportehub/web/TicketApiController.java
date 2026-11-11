package pa.soportehub.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
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

/** API de ESCRITURA para Postman/Bruno. Las lecturas están en TicketQueryApiController (P4). */
@RestController
@RequestMapping("/api/tickets")
public class TicketApiController {

    public record CreateBody(UUID categoryId, String subject, String description, Priority priority) {}
    public record AssignBody(UUID agentId, String reason) {}
    public record StatusBody(TicketStatus target, String reason) {}
    public record CommentBody(String body, boolean internal) {}
    public record ReasonBody(String reason) {}

    private final CreateTicketCommandHandler create;
    private final AssignTicketCommandHandler assign;
    private final ChangeTicketStatusCommandHandler changeStatus;
    private final AddCommentCommandHandler addComment;
    private final ReopenTicketCommandHandler reopen;

    public TicketApiController(CreateTicketCommandHandler create, AssignTicketCommandHandler assign,
                               ChangeTicketStatusCommandHandler changeStatus, AddCommentCommandHandler addComment,
                               ReopenTicketCommandHandler reopen) {
        this.create = create;
        this.assign = assign;
        this.changeStatus = changeStatus;
        this.addComment = addComment;
        this.reopen = reopen;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, UUID> create(@RequestBody CreateBody b) {
        return Map.of("id", create.handle(new CreateTicketCommand(b.categoryId(), b.subject(), b.description(), b.priority())));
    }

    @PostMapping("/{id}/assign")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assign(@PathVariable UUID id, @RequestBody AssignBody b) {
        assign.handle(new AssignTicketCommand(id, b.agentId(), b.reason()));
    }

    @PostMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void status(@PathVariable UUID id, @RequestBody StatusBody b) {
        changeStatus.handle(new ChangeTicketStatusCommand(id, b.target(), b.reason()));
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void comment(@PathVariable UUID id, @RequestBody CommentBody b) {
        addComment.handle(new AddCommentCommand(id, b.body(), b.internal()));
    }

    @PostMapping("/{id}/reopen")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reopen(@PathVariable UUID id, @RequestBody ReasonBody b) {
        reopen.handle(new ReopenTicketCommand(id, b.reason()));
    }
}
