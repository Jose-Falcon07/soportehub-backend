package com.soportehub.backend.application.dto.query;

import com.soportehub.backend.domain.exception.BusinessRuleException;
import com.soportehub.backend.domain.model.TicketStatus;

public record TicketListQuery(String status, int page, int size) {

  public TicketListQuery {
    status = (status == null || status.isBlank()) ? null : status.trim();
    if (status != null) {
      try {
        TicketStatus.valueOf(status);
      } catch (IllegalArgumentException e) {
        throw new BusinessRuleException("Estado no válido");
      }
    }
    page = Math.max(page, 0);
    size = Math.min(Math.max(size, 1), 50);
  }

}
