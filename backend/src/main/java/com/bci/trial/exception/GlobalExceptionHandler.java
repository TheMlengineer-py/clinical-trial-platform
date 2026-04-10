package com.bci.trial.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

/**
 * Central error handler for all controllers.
 *
 * <p>Converts every exception into a consistent JSON error envelope so the
 * React frontend can always parse failures identically, regardless of which
 * layer threw the error.
 *
 * <p>Response shape (all errors):
 * <pre>
 * {
 *   "timestamp": "2024-05-01T10:00:00Z",
 *   "status":    409,
 *   "error":     "Conflict",
 *   "message":   "Study 3 is not OPEN for recruitment"
 * }
 * </pre>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 Not Found ─────────────────────────────────────────────────────────

    @ExceptionHandler({StudyNotFoundException.class, PatientNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── 409 Conflict — business rule violations ───────────────────────────────

    @ExceptionHandler({
        InvalidStateTransitionException.class,
        EnrollmentCapacityException.class,
        PatientAlreadyEnrolledException.class,
        StudyNotOpenException.class,
        StudyDeletionNotAllowedException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── 422 Unprocessable — eligibility failure ───────────────────────────────

    @ExceptionHandler(EligibilityException.class)
    public ResponseEntity<ErrorResponse> handleEligibility(EligibilityException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    // ── 400 Bad Request — @Valid DTO validation failures ─────────────────────

    /**
     * Triggered when @Valid fails on a @RequestBody DTO.
     * Collects all field-level messages into a single semicolon-delimited string
     * so the frontend can display each violation separately if needed.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .toList();
        return build(HttpStatus.BAD_REQUEST, String.join("; ", errors));
    }

    // ── 400 Bad Request — @Validated path/query param violations ─────────────

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ── 500 Internal Server Error — unexpected failures ───────────────────────

    /**
     * Catch-all handler. Never exposes internal stack traces to the client —
     * only a generic message. The real error is available in server logs.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    // ── Private builder ───────────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
            .body(new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message
            ));
    }

    /** Immutable error envelope serialised to JSON by Jackson. */
    public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message
    ) {}
}
