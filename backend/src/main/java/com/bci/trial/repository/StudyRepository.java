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
 * <p>All queries filter on {@code deletedAt IS NULL} to honour soft deletes.
 * The pessimistic-lock query must only be called inside an active
 * {@code @Transactional} method.
 */
public interface StudyRepository extends JpaRepository<Study, Long> {

    /**
     * Returns all active (non-deleted) studies as a page.
     */
    @Query("SELECT s FROM Study s WHERE s.deletedAt IS NULL")
    Page<Study> findAllActive(Pageable pageable);

    /**
     * Returns active studies filtered by lifecycle status.
     */
    @Query("SELECT s FROM Study s WHERE s.status = :status AND s.deletedAt IS NULL")
    Page<Study> findByStatus(@Param("status") StudyStatus status, Pageable pageable);

    /**
     * Finds an active study by ID.
     */
    @Query("SELECT s FROM Study s WHERE s.id = :id AND s.deletedAt IS NULL")
    Optional<Study> findActiveById(@Param("id") Long id);

    /**
     * Acquires a pessimistic write lock on an active study row.
     * Must only be called within a {@code @Transactional} context.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Study s WHERE s.id = :id AND s.deletedAt IS NULL")
    Optional<Study> findByIdForUpdate(@Param("id") Long id);
}
