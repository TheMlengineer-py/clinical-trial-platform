package com.bci.trial.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Inbound payload for {@code POST /api/patients} and {@code PUT /api/patients/{id}}.
 *
 * <p>Note: {@code enrolledStudyId} is intentionally absent here — enrolment
 * is managed exclusively through {@code POST /api/recruitment}, not by
 * directly patching the patient record.
 */
@Data
public class PatientRequest {

    /** Patient's full name — displayed in the table and detail view. */
    @NotBlank(message = "Name is required")
    private String name;

    /** Age in years — validated for realism before storage. */
    @Min(value = 0,   message = "Age must be non-negative")
    @Max(value = 130, message = "Age value is not realistic")
    private int age;

    /**
     * Medical condition string — used for table filtering and eligibility checks.
     * Should match the values used in study {@code eligibilityCriteria}.
     * Example: "Breast cancer", "NSCLC", "Colorectal"
     */
    @NotBlank(message = "Condition is required")
    private String condition;
}
