package com.inventoryhub.common;

import jakarta.validation.ConstraintViolationException;
import com.inventoryhub.inventory.InsufficientStockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(
                ex.getCode().name(),
                ex.getMessage(),
                CorrelationId.currentOrNew(),
                List.of()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldErrorResponse> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();

        return ResponseEntity.badRequest().body(new ErrorResponse(
                ApiErrorCode.VALIDATION_ERROR.name(),
                "Request validation failed",
                CorrelationId.currentOrNew(),
                errors
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<FieldErrorResponse> errors = ex.getConstraintViolations().stream()
                .map(error -> new FieldErrorResponse(error.getPropertyPath().toString(), error.getMessage()))
                .toList();

        return ResponseEntity.badRequest().body(new ErrorResponse(
                ApiErrorCode.VALIDATION_ERROR.name(),
                "Request validation failed",
                CorrelationId.currentOrNew(),
                errors
        ));
    }

    @ExceptionHandler(InsufficientStockException.class)
    ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                ApiErrorCode.CONFLICT.name(),
                "Insufficient stock for product " + ex.getProductId(),
                CorrelationId.currentOrNew(),
                List.of()
        ));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected request failure", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                ApiErrorCode.INTERNAL_ERROR.name(),
                "Unexpected server error",
                CorrelationId.currentOrNew(),
                List.of()
        ));
    }

    private FieldErrorResponse toFieldError(FieldError error) {
        return new FieldErrorResponse(error.getField(), error.getDefaultMessage());
    }
}
