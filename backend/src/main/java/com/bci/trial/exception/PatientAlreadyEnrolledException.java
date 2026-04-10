package com.bci.trial.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a recruitment attempt targets a patient who is already
 * enrolled in another study. A patient may only participate in one study
 * at a time — enforced in {@code RecruitmentService} before acquiring the lock.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class PatientAlreadyEnrolledException extends RuntimeException {
    public PatientAlreadyEnrolledException(Long patientId) {
        super("Patient " + patientId + " is already enrolled in a study");
    }
}
