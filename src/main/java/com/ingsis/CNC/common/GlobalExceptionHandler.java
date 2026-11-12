package com.ingsis.CNC.common;

import com.ingsis.CNC.permissions.PermissionAlreadyExistsException;
import com.ingsis.CNC.permissions.dto.ErrorResponse;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(PermissionAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handlePermissionAlreadyExists(
      PermissionAlreadyExistsException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            new ErrorResponse(
                "ALREADY_EXISTS",
                ex.getReason() != null ? ex.getReason() : ex.getMessage(),
                List.of()));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
      DataIntegrityViolationException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorResponse("ALREADY_EXISTS", "Permission already exists", List.of()));
  }
}
