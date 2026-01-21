package com.davidcerdeiro.documind.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // RAG Handler
    @ExceptionHandler(NoDocumentsException.class)
    public ResponseEntity<Map<String, Object>> handleNoDocuments(NoDocumentsException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Information Not Found", ex.getMessage());
    }

    // Upload Handler
    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidFileType(InvalidFileTypeException ex) {
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type", ex.getMessage());
    }

    // Generic Handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred: " + ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = Map.of(
            "timestamp", LocalDateTime.now(),
            "status", status.value(),
            "error", error,
            "message", message
        );
        return new ResponseEntity<>(body, status);
    }
}