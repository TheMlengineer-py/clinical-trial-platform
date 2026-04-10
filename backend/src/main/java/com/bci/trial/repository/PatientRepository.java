package com.bci.trial.repository;

import com.bci.trial.domain.Patient;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

/**
 * JPA repository for {@link Patient} entities.
 *
 * <p>The default page sort is by {@code recruitedAt DESC} to satisfy the
 * assessment requirement of "sorted by most recently recruited". Patients
 * who have never been recruited (null recruitedAt) appear at the bottom.
 */
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Case-insensitive partial match on the condition field.
     * Drives the condition filter dropdown on the patients table.
     * Example: condition="cancer" matches "Breast cancer", "Pancreatic cancer".
     *
     * @param condition partial condition string to match
     * @param pageable  pagination and sort parameters
     */
    @Query("SELECT p FROM Patient p WHERE LOWER(p.condition) LIKE LOWER(CONCAT('%', :condition, '%'))")
    Page<Patient> findByConditionContainingIgnoreCase(
        @Param("condition") String condition, Pageable pageable);

    /**
     * Returns all patients sorted by most recently recruited (DESC).
     * Patients with null recruitedAt (never enrolled) appear last.
     * Used as the default view when no condition filter is applied.
     *
     * @param pageable pagination parameters (sort is overridden by the JPQL ORDER BY)
     */
    @Query("SELECT p FROM Patient p ORDER BY p.recruitedAt DESC NULLS LAST")
    Page<Patient> findAllSortedByRecruitedAt(Pageable pageable);

    /**
     * Quick existence check used by RecruitmentService to detect double-enrolment
     * before acquiring the expensive pessimistic lock.
     *
     * @param id patient ID to check
     * @return true if the patient currently has a non-null enrolledStudyId
     */
    boolean existsByIdAndEnrolledStudyIdIsNotNull(Long id);
}
