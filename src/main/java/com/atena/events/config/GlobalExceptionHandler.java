package com.atena.events.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Bean Validation errors (from @Valid) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
            fieldErrors.put(err.getField(), err.getDefaultMessage())
        );

        // Retorna a primeira mensagem de erro como "error" principal
        String firstMessage = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getDefaultMessage())
                .orElse("Dados inválidos.");

        Map<String, Object> body = new HashMap<>();
        body.put("error", firstMessage);
        body.put("fields", fieldErrors);

        return ResponseEntity.badRequest().body(body);
    }

    /** ResponseStatusException (thrown from services/controllers) */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex) {
        String msg = ex.getReason() != null ? ex.getReason() : "Erro na requisição.";
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", msg));
    }

    /** JSON malformado ou tipo incompatível (ex.: data no formato errado) */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(HttpMessageNotReadableException ex) {
        String cause = ex.getMostSpecificCause().getMessage();
        String msg = "Dado inválido recebido.";

        if (cause != null && cause.contains("LocalDateTime")) {
            msg = "Formato de data inválido. Use o formato: yyyy-MM-ddTHH:mm:ss";
        } else if (cause != null) {
            msg = "Dado inválido: " + cause;
        }

        return ResponseEntity.badRequest().body(Map.of("error", msg));
    }

    /** Qualquer outro RuntimeException não tratado */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Erro interno inesperado.";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", msg));
    }
}
