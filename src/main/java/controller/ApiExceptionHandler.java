package controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> manejarResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request
    ) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", exception.getStatusCode().value());
        error.put("error", exception.getStatusCode().toString());
        error.put("message", exception.getReason());
        error.put("path", request.getRequestURI());

        return ResponseEntity.status(exception.getStatusCode()).body(error);
    }
}
