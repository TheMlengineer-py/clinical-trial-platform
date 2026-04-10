package com.bci.trial.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown by {@code StudyService.getOrThrow()} when no study exists for the given ID.
 * {@code @ResponseStatus} maps this to HTTP 404 when caught by {@code GlobalExceptionHandler}.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class StudyNotFoundException extends RuntimeException {
    public StudyNotFoundException(Long id) {
        super("Study not found with id: " + id);
    }
}
