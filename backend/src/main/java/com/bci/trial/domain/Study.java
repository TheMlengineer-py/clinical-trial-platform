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
 * <p>{@code deletedAt} supports soft deletes — records are never physically
 * removed from the database, allowing audit trail preservation and recovery.
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

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StudyStatus status = StudyStatus.DRAFT;

    @Column(nullable = false)
    private int maxEnrollment;

    @Column(nullable = false)
    @Builder.Default
    private int currentEnrollment = 0;

    private String eligibilityCriteria;

    private Instant lastRecruitedAt;

    @Version
    private Long version;

    /**
     * Soft-delete timestamp. NULL means the record is active.
     * Set by {@code StudyService.delete()} instead of issuing a DELETE statement.
     */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    /** Convenience check used by service and repository layers. */
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
