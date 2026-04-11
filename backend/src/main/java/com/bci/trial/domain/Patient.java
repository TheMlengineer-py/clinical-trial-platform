package com.bci.trial.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * JPA entity representing a patient eligible for clinical trial recruitment.
 *
 * <p>{@code deletedAt} supports soft deletes — records are never physically
 * removed, preserving recruitment history and audit trail.
 */
@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false)
    private String condition;

    private Long enrolledStudyId;

    private Instant recruitedAt;

    /**
     * Soft-delete timestamp. NULL means the record is active.
     * Set by {@code PatientService.delete()} instead of issuing a DELETE statement.
     */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    /** Convenience check used by service and repository layers. */
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
