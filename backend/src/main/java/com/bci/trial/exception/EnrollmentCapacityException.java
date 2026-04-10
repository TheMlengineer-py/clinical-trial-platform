package com.bci.trial.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a recruitment attempt would push {@code currentEnrollment}
 * beyond {@code maxEnrollment}.
 *
 * <p>This check occurs inside the pessimistic-locked transaction in
 * {@code RecruitmentService} — it is the last line of defence against
 * over-enrollment in concurrent scenarios.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class EnrollmentCapacityException extends RuntimeException {
    public EnrollmentCapacityException(Long studyId) {
        super("Study " + studyId + " has reached its maximum enrollment capacity");
    }
}
