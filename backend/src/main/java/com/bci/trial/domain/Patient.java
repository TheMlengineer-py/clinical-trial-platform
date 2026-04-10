package com.bci.trial.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * JPA entity representing a patient eligible for clinical trial recruitment.
 *
 * <p>A patient may be enrolled in at most one study at any point in time.
 * {@code enrolledStudyId} is {@code null} when the patient has no active enrolment.
 *
 * <p>We store the study ID directly rather than a {@code @ManyToOne} relation
 * to keep the entity lightweight and avoid cascade complexity when studies
 * are archived or soft-deleted in future iterations.
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

    /** Full name displayed in the patient table and detail view. */
    @Column(nullable = false)
    private String name;

    /**
     * Age in years.
     * Used for eligibility criteria matching (e.g. {@code age>18}).
     */
    @Column(nullable = false)
    private int age;

    /**
     * Medical condition (e.g. "Breast cancer", "NSCLC").
     * Used as a filter parameter on {@code GET /api/patients?condition=...}
     * and matched against the study's {@code eligibilityCriteria}.
     */
    @Column(nullable = false)
    private String condition;

    /**
     * ID of the study this patient is currently enrolled in.
     * {@code null} when the patient has no active enrolment.
     * Set by {@code RecruitmentService.recruit()} and cleared on unenrolment.
     */
    private Long enrolledStudyId;

    /**
     * Timestamp of the most recent recruitment action for this patient.
     * Drives the default "sort by most recent" ordering on the patients table.
     * {@code null} for patients who have never been recruited.
     */
    private Instant recruitedAt;
}
