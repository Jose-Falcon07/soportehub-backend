package com.soportehub.backend.interfaces.web;

import java.util.function.Function;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.soportehub.backend.application.dto.query.PageResult;
import com.soportehub.backend.application.dto.query.TicketListQuery;
import com.soportehub.backend.application.dto.query.TicketSummaryView;
import com.soportehub.backend.application.service.query.AgentInboxQueryHandler;
import com.soportehub.backend.application.service.query.MyTicketsQueryHandler;
import com.soportehub.backend.application.service.query.QueueQueryHandler;
import com.soportehub.backend.domain.model.TicketStatus;

@Controller
public class TicketListController {

  private final AgentInboxQueryHandler inbox;
  private final QueueQueryHandler queue;
  private final MyTicketsQueryHandler mine;

  public TicketListController(AgentInboxQueryHandler inbox, QueueQueryHandler queue, MyTicketsQueryHandler mine) {
    this.inbox = inbox;
    this.queue = queue;
    this.mine = mine;
  }

  @GetMapping("/inbox")
  public String inbox(@RequestParam(required = false) String status, @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size, Model model) {
    return render(model, "/inbox", "list.inbox.title", inbox::handle, new TicketListQuery(status, page, size));
  }

  @GetMapping("/queue")
  public String queue(@RequestParam(required = false) String status, @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size, Model model) {
    return render(model, "/queue", "list.queue.title", queue::handle, new TicketListQuery(status, page, size));
  }

  @GetMapping("/my-tickets")
  public String myTickets(@RequestParam(required = false) String status, @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size, Model model) {
    return render(model, "/my-tickets", "list.mine.title", mine::handle, new TicketListQuery(status, page, size));
  }

  private String render(Model model, String basePath, String titleKey,
      Function<TicketListQuery, PageResult<TicketSummaryView>> handler, TicketListQuery q) {
    model.addAttribute("result", handler.apply(q));
    model.addAttribute("basePath", basePath);
    model.addAttribute("titleKey", titleKey);
    model.addAttribute("status", q.status());
    model.addAttribute("size", q.size());
    model.addAttribute("statuses", TicketStatus.values());
    return "ticket-list";
  }

}
