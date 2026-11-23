package com.soportehub.backend.application.service.query;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.soportehub.backend.application.dto.query.PageResult;
import com.soportehub.backend.application.dto.query.TicketListQuery;
import com.soportehub.backend.application.dto.query.TicketSummaryView;
import com.soportehub.backend.infrastructure.redis.InboxCache;
import com.soportehub.backend.infrastructure.security.CurrentUser;
import com.soportehub.backend.infrastructure.security.TenantContext;

@Service
public class AgentInboxQueryHandler {

  private final TicketListReader reader;
  private final InboxCache cache;

  public AgentInboxQueryHandler(TicketListReader reader, InboxCache cache) {
    this.reader = reader;
    this.cache = cache;
  }

  @PreAuthorize("hasAnyRole('COMPANY_ADMIN','SUPERVISOR','AGENT')")
  public PageResult<TicketSummaryView> handle(TicketListQuery q) {
    UUID org = TenantContext.require();
    UUID me = CurrentUser.get().userId();
    String key = "agent:" + me + ":" + q.status() + ":" + q.page() + ":" + q.size();
    return cache.getOrLoad(org, key, () -> reader.read(org, me, null, q.status(), q.page(), q.size()));
  }
}
