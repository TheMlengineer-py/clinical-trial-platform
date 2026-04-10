package com.bci.trial.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when the patient's age or condition does not satisfy the study's
 * {@code eligibilityCriteria} string.
 *
 * <p>HTTP 422 Unprocessable Entity is more precise than 409 here —
 * the request is well-formed but the business data fails validation.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class EligibilityException extends RuntimeException {
    public EligibilityException(Long patientId, Long studyId) {
        super("Patient " + patientId + " does not meet eligibility criteria for study " + studyId);
    }
}
