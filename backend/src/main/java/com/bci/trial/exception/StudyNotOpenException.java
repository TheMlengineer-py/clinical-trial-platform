package com.bci.trial.exception;

/**
 * Thrown when an enrollment attempt is made against a Study
 * that is not in OPEN status.
 */
public class StudyNotOpenException extends RuntimeException {

    public StudyNotOpenException(Long studyId) {
        super("Study " + studyId + " is not OPEN for recruitment");
    }

    public StudyNotOpenException(String message) {
        super(message);
    }
}
