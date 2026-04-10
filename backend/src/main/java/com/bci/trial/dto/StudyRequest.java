package com.bci.trial.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Inbound payload for {@code POST /api/studies} and {@code PUT /api/studies/{id}}.
 *
 * <p>Bean Validation annotations are processed by {@code @Valid} in
 * {@code StudyController} before the request reaches the service layer.
 */
@Data
public class StudyRequest {

    /** Study title — required, displayed in the study table. */
    @NotBlank(message = "Title is required")
    private String title;

    /**
     * Maximum number of patients allowed in the study.
     * Must be at least 1 — a study with zero slots is meaningless.
     */
    @Min(value = 1, message = "maxEnrollment must be at least 1")
    private int maxEnrollment;

    /**
     * Optional eligibility rules as a comma-separated string.
     * Format: {@code key operator value}
     * Supported operators: {@code >}, {@code <}, {@code =}
     * Example: {@code "age>18,condition=NSCLC"}
     * Parsed at recruitment time by {@code EligibilityChecker} in PatientService.
     */
    private String eligibilityCriteria;
}
