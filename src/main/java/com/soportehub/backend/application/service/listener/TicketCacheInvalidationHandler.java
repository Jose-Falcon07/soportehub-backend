package com.soportehub.backend.application.service.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.soportehub.backend.domain.event.TicketChangedEvent;
import com.soportehub.backend.infrastructure.redis.InboxCache;

@Component
public class TicketCacheInvalidationHandler {

  private final InboxCache cache;

  public TicketCacheInvalidationHandler(InboxCache cache) {
    this.cache = cache;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(TicketChangedEvent event) {
      cache.evictOrganization(event.organizationId());
  }

}
