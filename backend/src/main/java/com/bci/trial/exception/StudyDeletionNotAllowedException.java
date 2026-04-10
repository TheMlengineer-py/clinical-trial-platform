package com.bci.trial.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a delete is attempted on an OPEN study.
 * An open study has active enrolled patients, so deletion is blocked
 * to preserve referential integrity. The study must be CLOSED first.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class StudyDeletionNotAllowedException extends RuntimeException {
    public StudyDeletionNotAllowedException(Long studyId) {
        super("Cannot delete study " + studyId + " because it is currently OPEN");
    }
}
