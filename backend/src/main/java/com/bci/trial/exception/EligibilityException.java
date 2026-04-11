package com.bci.trial.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a patient does not satisfy the study's eligibility criteria.
 *
 * <p>HTTP 422 Unprocessable Entity — the request is well-formed but the
 * business data fails validation.
 *
 * <p>The {@code reason} field carries the specific rule that failed,
 * produced by {@code EligibilityEngine} for clear error messages.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class EligibilityException extends RuntimeException {

    /**
     * Generic constructor — used when no specific reason is available.
     */
    public EligibilityException(Long patientId, Long studyId) {
        super("Patient " + patientId +
              " does not meet eligibility criteria for study " + studyId);
    }

    /**
     * Detailed constructor — includes the specific rule that failed.
     *
     * @param patientId the patient ID
     * @param studyId   the study ID
     * @param reason    human-readable explanation from EligibilityEngine
     */
    public EligibilityException(Long patientId, Long studyId, String reason) {
        super("Patient " + patientId +
              " does not meet eligibility criteria for study " + studyId +
              ": " + reason);
    }
}
