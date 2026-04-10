package com.bci.trial.dto;

import java.time.Instant;

/**
 * Outbound patient payload returned by all patient endpoints.
 *
 * <p>{@code enrolledStudyId} is {@code null} when the patient is not
 * currently enrolled in any study — the frontend renders this as "None".
 *
 * <p>{@code recruitedAt} is {@code null} for patients who have never been
 * recruited; they appear at the bottom of the default sort order.
 */
public record PatientResponse(
    Long id,
    String name,
    int age,
    String condition,
    Long enrolledStudyId,
    Instant recruitedAt
) {}
