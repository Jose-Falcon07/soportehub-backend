package com.soportehub.backend.interfaces.web;

import com.soportehub.backend.application.dto.command.ChangeTicketStatusCommand;
import com.soportehub.backend.application.dto.command.CreateTicketCommand;
import com.soportehub.backend.application.dto.command.ReopenTicketCommand;
import com.soportehub.backend.application.service.command.ChangeTicketStatusCommandHandler;
import com.soportehub.backend.application.service.command.CreateTicketCommandHandler;
import com.soportehub.backend.application.service.command.ReopenTicketCommandHandler;
import com.soportehub.backend.domain.model.Priority;
import com.soportehub.backend.domain.model.TicketStatus;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Ciclo de vida del ticket desde las pantallas: crear, cambiar estado y reabrir. */
@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final CreateTicketCommandHandler create;
    private final ChangeTicketStatusCommandHandler changeStatus;
    private final ReopenTicketCommandHandler reopen;

    public TicketController(CreateTicketCommandHandler create, ChangeTicketStatusCommandHandler changeStatus,
                            ReopenTicketCommandHandler reopen) {
        this.create = create;
        this.changeStatus = changeStatus;
        this.reopen = reopen;
    }

    @PostMapping
    public String create(@RequestParam UUID categoryId, @RequestParam String subject,
                         @RequestParam String description, @RequestParam Priority priority) {
        UUID id = create.handle(new CreateTicketCommand(categoryId, subject, description, priority));
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/status")
    public String status(@PathVariable UUID id, @RequestParam TicketStatus target,
                         @RequestParam(required = false) String reason) {
        changeStatus.handle(new ChangeTicketStatusCommand(id, target, reason));
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/reopen")
    public String reopen(@PathVariable UUID id, @RequestParam(required = false) String reason) {
        reopen.handle(new ReopenTicketCommand(id, reason));
        return "redirect:/tickets/" + id;
    }
}
