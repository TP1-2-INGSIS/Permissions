package com.ingsis.CNC.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.ingsis.CNC.permissions.PermissionAlreadyExistsException;
import com.ingsis.CNC.permissions.dto.ErrorResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handlesPermissionAlreadyExistsException() {
    PermissionAlreadyExistsException ex =
        new PermissionAlreadyExistsException("Permission already exists");

    ResponseEntity<ErrorResponse> response = handler.handlePermissionAlreadyExists(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody())
        .isEqualTo(new ErrorResponse("ALREADY_EXISTS", "Permission already exists", List.of()));
  }

  @Test
  void handlesDataIntegrityViolationException() {
    DataIntegrityViolationException ex =
        new DataIntegrityViolationException("Unique index violation");

    ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody())
        .isEqualTo(new ErrorResponse("ALREADY_EXISTS", "Permission already exists", List.of()));
  }
}
