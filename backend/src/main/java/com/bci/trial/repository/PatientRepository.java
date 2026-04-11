package com.bci.trial.repository;

import com.bci.trial.domain.Patient;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

/**
 * JPA repository for {@link Patient} entities.
 *
 * <p>All queries filter on {@code deletedAt IS NULL} to honour soft deletes.
 */
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Case-insensitive partial match on condition — active patients only.
     */
    @Query("SELECT p FROM Patient p WHERE LOWER(p.condition) " +
           "LIKE LOWER(CONCAT('%', :condition, '%')) " +
           "AND p.deletedAt IS NULL")
    Page<Patient> findByConditionContainingIgnoreCase(
        @Param("condition") String condition, Pageable pageable);

    /**
     * All active patients sorted by most recently recruited.
     */
    @Query("SELECT p FROM Patient p WHERE p.deletedAt IS NULL " +
           "ORDER BY p.recruitedAt DESC NULLS LAST")
    Page<Patient> findAllSortedByRecruitedAt(Pageable pageable);

    /**
     * Existence check for double-enrolment detection — active patients only.
     */
    @Query("SELECT COUNT(p) > 0 FROM Patient p " +
           "WHERE p.id = :id AND p.enrolledStudyId IS NOT NULL " +
           "AND p.deletedAt IS NULL")
    boolean existsByIdAndEnrolledStudyIdIsNotNull(@Param("id") Long id);
}
