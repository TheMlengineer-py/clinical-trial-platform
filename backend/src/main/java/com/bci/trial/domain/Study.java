package com.bci.trial.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * JPA entity representing a clinical research study.
 *
 * <p>The {@code version} field provides optimistic locking as a secondary
 * safety net. The primary concurrency guard for the last-slot boundary
 * is pessimistic ({@code SELECT FOR UPDATE}) inside {@code RecruitmentService}.
 *
 * <p>Business rules are enforced at the service layer, not here — the
 * entity is a pure data holder with no behaviour methods.
 */
@Entity
@Table(name = "studies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Study {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable title shown in the study table. */
    @Column(nullable = false)
    private String title;

    /**
     * Current lifecycle state. Stored as a string for readability in H2 console.
     * Defaults to DRAFT via {@code @Builder.Default}.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StudyStatus status = StudyStatus.DRAFT;

    /** Hard cap on the number of patients that may be enrolled. */
    @Column(nullable = false)
    private int maxEnrollment;

    /**
     * Running count of enrolled patients.
     * Incremented atomically inside a pessimistic-locked transaction
     * in {@code RecruitmentService} to prevent race conditions.
     */
    @Column(nullable = false)
    @Builder.Default
    private int currentEnrollment = 0;

    /**
     * Free-text eligibility rule string parsed by {@code EligibilityChecker}.
     * Format: comma-separated key operator value pairs.
     * Example: {@code "age>18,condition=NSCLC"}
     */
    private String eligibilityCriteria;

    /**
     * Timestamp of the most recent patient recruitment.
     * Updated on every successful recruitment; used for default sort order.
     */
    private Instant lastRecruitedAt;

    /**
     * JPA optimistic lock version.
     * Incremented automatically on every update — provides a secondary
     * race-condition guard on top of the pessimistic lock.
     */
    @Version
    private Long version;
}
