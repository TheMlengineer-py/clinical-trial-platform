package com.bci.trial.domain;

/**
 * Enum representing the lifecycle states of a clinical study.
 *
 * <p>Transitions are strictly forward and one-step at a time:
 * <pre>
 *   DRAFT → OPEN → CLOSED → ARCHIVED
 * </pre>
 *
 * <p>The {@link #isValidTransition(StudyStatus)} method is the single
 * source of truth for what is allowed. StudyService delegates to it
 * rather than duplicating switch logic.
 */
public enum StudyStatus {
    /** Study is being configured — not yet accepting patients. */
    DRAFT,

    /** Study is live — the only state that accepts patient recruitment. */
    OPEN,

    /** Recruitment window has ended — no new patients can be enrolled. */
    CLOSED,

    /** Study is fully completed and read-only. Terminal state. */
    ARCHIVED;

    /**
     * Returns {@code true} if transitioning from {@code this} state
     * to {@code next} is a legal one-step forward move.
     *
     * @param next the requested target state
     * @return true only for the four valid forward transitions
     */
    public boolean isValidTransition(StudyStatus next) {
        return switch (this) {
            case DRAFT    -> next == OPEN;
            case OPEN     -> next == CLOSED;
            case CLOSED   -> next == ARCHIVED;
            case ARCHIVED -> false; // terminal — no exit
        };
    }
}
