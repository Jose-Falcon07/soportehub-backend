package com.soportehub.backend.application.dto.query;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record PageResult<T>(List<T> content, int page, int size, long total) {

  @JsonIgnore
  public int totalPages() {
    return (int) Math.ceil((double) total / size);
  }
}
