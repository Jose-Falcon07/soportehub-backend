package com.soportehub.backend.application.service.assignment;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class AssignmentStrategyResolver {

  private final Map<String, AgentAssignmentStrategy> byCode;

  public AssignmentStrategyResolver(List<AgentAssignmentStrategy> all) {
    this.byCode = all.stream().collect(Collectors.toMap(AgentAssignmentStrategy::code, Function.identity()));
  }

  public AgentAssignmentStrategy byCode(String code) {
    return byCode.getOrDefault(code, byCode.get("MANUAL"));
  }
}
