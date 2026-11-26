package com.soportehub.backend.interfaces.rest;

import com.soportehub.backend.application.dto.command.ChangeTicketStatusCommand;
import com.soportehub.backend.application.dto.command.CreateTicketCommand;
import com.soportehub.backend.application.dto.command.ReopenTicketCommand;
import com.soportehub.backend.application.service.command.ChangeTicketStatusCommandHandler;
import com.soportehub.backend.application.service.command.CreateTicketCommandHandler;
import com.soportehub.backend.application.service.command.ReopenTicketCommandHandler;
import com.soportehub.backend.domain.model.Priority;
import com.soportehub.backend.domain.model.TicketStatus;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** API de escritura (ciclo de vida) para Postman/Bruno. **/
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketApiController {

    public record CreateBody(UUID categoryId, String subject, String description, Priority priority) {}
    public record StatusBody(TicketStatus target, String reason) {}
    public record ReasonBody(String reason) {}

    private final CreateTicketCommandHandler create;
    private final ChangeTicketStatusCommandHandler changeStatus;
    private final ReopenTicketCommandHandler reopen;

    public TicketApiController(CreateTicketCommandHandler create, ChangeTicketStatusCommandHandler changeStatus,
                               ReopenTicketCommandHandler reopen) {
        this.create = create;
        this.changeStatus = changeStatus;
        this.reopen = reopen;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, UUID> create(@RequestBody CreateBody b) {
        return Map.of("id", create.handle(new CreateTicketCommand(b.categoryId(), b.subject(), b.description(), b.priority())));
    }

    @PostMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void status(@PathVariable UUID id, @RequestBody StatusBody b) {
        changeStatus.handle(new ChangeTicketStatusCommand(id, b.target(), b.reason()));
    }

    @PostMapping("/{id}/reopen")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reopen(@PathVariable UUID id, @RequestBody ReasonBody b) {
        reopen.handle(new ReopenTicketCommand(id, b.reason()));
    }
}
