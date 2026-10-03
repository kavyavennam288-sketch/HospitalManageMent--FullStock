package com.medsync.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// @RestControllerAdvice intercepts exceptions from ALL controllers.
// Without this, Spring returns an HTML error page — useless for a JSON API.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles @Valid validation failures
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        // Collect all field-level errors into a readable map
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(fieldName, message);
        });

        // Example response:
        // { "email": "Must be a valid email address", "password": "..." }
        return ResponseEntity.badRequest().body(
            buildError(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors)
        );
    }

    // Handles wrong password or unknown email at login
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex) {
        // Intentionally vague message — don't tell attackers which part was wrong
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            buildError(HttpStatus.UNAUTHORIZED, "Invalid email or password", null)
        );
    }

    // Handles our custom business exceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex) {
        return ResponseEntity.badRequest().body(
            buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), null)
        );
    }

    private Map<String, Object> buildError(
            HttpStatus status, String message, Object details) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        if (details != null) body.put("details", details);
        return body;
    }
}