package com.bci.trial.dto;

import com.bci.trial.domain.StudyStatus;
import java.time.Instant;

/**
 * Outbound study payload returned by all study endpoints.
 *
 * <p>Declared as a Java record for compile-time immutability.
 * Jackson serialises records natively — no annotations needed.
 * Mapped from {@code Study} entity inside {@code StudyService.toResponse()}.
 */
public record StudyResponse(
    Long id,
    String title,
    StudyStatus status,
    int maxEnrollment,
    int currentEnrollment,
    String eligibilityCriteria,

    /**
     * Timestamp of the most recent patient recruitment into this study.
     * {@code null} when no patient has been recruited yet.
     * Serialised as ISO-8601 string by Jackson.
     */
    Instant lastRecruitedAt
) {}
