package controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;

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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> manejarJsonInvalido(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return crearError(HttpStatus.BAD_REQUEST, "El cuerpo JSON no es válido.", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> manejarIntegridad(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return crearError(HttpStatus.CONFLICT, "La operacion viola una relacion existente en la base de datos.", request);
    }

    private ResponseEntity<Map<String, Object>> crearError(
            HttpStatus estado,
            String mensaje,
            HttpServletRequest request
    ) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", estado.value());
        error.put("error", estado.getReasonPhrase());
        error.put("message", mensaje);
        error.put("path", request.getRequestURI());
        return ResponseEntity.status(estado).body(error);
    }
}
