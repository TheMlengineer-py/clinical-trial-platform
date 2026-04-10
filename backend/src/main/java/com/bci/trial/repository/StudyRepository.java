package com.bci.trial.repository;

import com.bci.trial.domain.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

/**
 * JPA repository for {@link Study} entities.
 *
 * <p>Spring Data generates all implementations at runtime.
 * The pessimistic-lock query ({@link #findByIdForUpdate}) is used exclusively
 * by {@code RecruitmentService} to prevent the last-slot race condition —
 * it must only be called inside an active {@code @Transactional} method.
 */
public interface StudyRepository extends JpaRepository<Study, Long> {

    /**
     * Returns a page of studies filtered by lifecycle status.
     * Drives the status dropdown on the study table.
     *
     * @param status   the target status to filter by
     * @param pageable pagination and sort parameters
     */
    Page<Study> findByStatus(StudyStatus status, Pageable pageable);

    /**
     * Acquires a pessimistic write lock on the study row for the duration
     * of the calling transaction.
     *
     * <p>Without this lock, two concurrent recruitment requests could both
     * pass the capacity check and increment {@code currentEnrollment} beyond
     * {@code maxEnrollment} (classic check-then-act race condition).
     *
     * <p><strong>Must only be called within a {@code @Transactional} context.</strong>
     *
     * @param id the study ID to lock and retrieve
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Study s WHERE s.id = :id")
    Optional<Study> findByIdForUpdate(@Param("id") Long id);
}
