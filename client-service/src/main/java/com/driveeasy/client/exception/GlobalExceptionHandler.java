package com.driveeasy.client.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Catches the exceptions thrown anywhere in the controllers/services
 * and turns them into a clean JSON response with the right HTTP code.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req, null);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleUnavailable(ServiceUnavailableException ex, HttpServletRequest req) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), req, null);
    }

    /** @Valid failed: returns every invalid field with its message. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> details = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> details.put(e.getField(), e.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, "Donnees invalides", req, details);
    }

    /** Malformed JSON or unknown enum value (ex: "carburant": "GAZ"). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Corps de requete illisible ou valeur inconnue", req, null);
    }

    /** Wrong type in the URL (ex: /vehicules/abc) or missing query parameter. */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleBadParam(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Parametre invalide : " + ex.getMessage(), req, null);
    }

    /** Unknown URL (ex: /favicon.ico requested by the browser) -> 404 instead of 500. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Ressource introuvable", req, null);
    }

    /** Wrong HTTP method (ex: DELETE /vehicules) -> 405. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethod(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), req, null);
    }

    /** Anything unexpected -> 500 (logged with the stack trace). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex, HttpServletRequest req) {
        log.error("Erreur inattendue", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne du serveur", req, null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message,
                                                HttpServletRequest req, Map<String, String> details) {
        ErrorResponse body = new ErrorResponse(LocalDateTime.now(), status.value(),
                status.getReasonPhrase(), message, req.getRequestURI(), details);
        return ResponseEntity.status(status).body(body);
    }
}
