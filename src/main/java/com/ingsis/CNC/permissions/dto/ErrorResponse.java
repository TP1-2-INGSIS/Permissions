package com.ingsis.CNC.permissions.dto;

import java.util.List;

public record ErrorResponse(String error, String message, List<Object> details) {
  public ErrorResponse(String error, String message) {
    this(error, message, List.of());
  }
}
