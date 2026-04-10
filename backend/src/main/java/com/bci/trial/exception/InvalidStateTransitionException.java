package com.bci.trial.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested study lifecycle transition violates the
 * DRAFT → OPEN → CLOSED → ARCHIVED chain.
 *
 * <p>Examples that trigger this: DRAFT → CLOSED, OPEN → DRAFT, ARCHIVED → OPEN.
 * The message tells the caller exactly what transition was attempted.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(String from, String to) {
        super("Cannot transition study from " + from + " to " + to);
    }
}
