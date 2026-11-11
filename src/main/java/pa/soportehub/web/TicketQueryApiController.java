package pa.soportehub.web;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pa.soportehub.application.query.AgentInboxQueryHandler;
import pa.soportehub.application.query.MyTicketsQueryHandler;
import pa.soportehub.application.query.PageResult;
import pa.soportehub.application.query.QueueQueryHandler;
import pa.soportehub.application.query.TicketDetailQueryHandler;
import pa.soportehub.application.query.TicketDetailView;
import pa.soportehub.application.query.TicketListQuery;
import pa.soportehub.application.query.TicketSummaryView;

/** API de LECTURA para Postman/Bruno. */
@RestController
@RequestMapping("/api")
public class TicketQueryApiController {

    private final AgentInboxQueryHandler inbox;
    private final QueueQueryHandler queue;
    private final MyTicketsQueryHandler mine;
    private final TicketDetailQueryHandler detail;

    public TicketQueryApiController(AgentInboxQueryHandler inbox, QueueQueryHandler queue,
                                    MyTicketsQueryHandler mine, TicketDetailQueryHandler detail) {
        this.inbox = inbox;
        this.queue = queue;
        this.mine = mine;
        this.detail = detail;
    }

    @GetMapping("/inbox")
    public PageResult<TicketSummaryView> inbox(@RequestParam(required = false) String status,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        return inbox.handle(new TicketListQuery(status, page, size));
    }

    @GetMapping("/queue")
    public PageResult<TicketSummaryView> queue(@RequestParam(required = false) String status,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        return queue.handle(new TicketListQuery(status, page, size));
    }

    @GetMapping("/my-tickets")
    public PageResult<TicketSummaryView> myTickets(@RequestParam(required = false) String status,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return mine.handle(new TicketListQuery(status, page, size));
    }

    @GetMapping("/tickets/{id}")
    public TicketDetailView detail(@PathVariable UUID id) {
        return detail.handle(id);
    }
}
