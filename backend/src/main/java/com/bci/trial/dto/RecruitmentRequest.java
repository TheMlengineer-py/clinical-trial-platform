package com.bci.trial.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Inbound payload for {@code POST /api/recruitment}.
 *
 * <p>Both fields are mandatory — partial recruitment requests are rejected
 * at the validation layer before reaching {@code RecruitmentService}.
 * This prevents ambiguous partial state in the concurrency-sensitive recruit flow.
 */
@Data
public class RecruitmentRequest {

    /** ID of the patient to recruit. Must exist and not already be enrolled. */
    @NotNull(message = "patientId is required")
    private Long patientId;

    /** ID of the target study. Must exist and have status OPEN. */
    @NotNull(message = "studyId is required")
    private Long studyId;
}
