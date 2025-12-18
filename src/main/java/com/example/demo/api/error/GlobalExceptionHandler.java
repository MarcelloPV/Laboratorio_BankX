package com.example.demo.api.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiError> handleBusiness(BusinessException ex) {
    HttpStatus status = switch (ex.getCode()) {
      case "account_not_found" -> HttpStatus.NOT_FOUND;
      case "risk_rejected", "insufficient_funds" -> HttpStatus.BAD_REQUEST;
      default -> HttpStatus.BAD_REQUEST;
    };

    return ResponseEntity.status(status).body(new ApiError(ex.getCode(), ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneric(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiError("internal_error", "Ocurrió un error inesperado XD"));
  }
}
