package com.bci.trial.service;

import com.bci.trial.domain.*;
import com.bci.trial.dto.*;
import com.bci.trial.exception.*;
import com.bci.trial.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for study management.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Enforce the DRAFT → OPEN → CLOSED → ARCHIVED lifecycle chain</li>
 *   <li>Block deletion of studies in OPEN status</li>
 *   <li>Map between {@link Study} entities and {@link StudyResponse} DTOs</li>
 * </ul>
 *
 * <p>All public methods are {@code @Transactional} — reads use
 * {@code readOnly=true} for performance (Hibernate skips dirty-checking).
 */
@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;

    // ── Query methods ─────────────────────────────────────────────────────────

    /**
     * Returns a paginated list of studies, optionally filtered by status.
     *
     * @param status   filter by this status; pass {@code null} to return all
     * @param pageable pagination and sort parameters from the controller
     */
    @Transactional(readOnly = true)
    public Page<StudyResponse> findAll(StudyStatus status, Pageable pageable) {
        Page<Study> page = (status != null)
            ? studyRepository.findByStatus(status, pageable)
            : studyRepository.findAll(pageable);
        return page.map(this::toResponse);
    }

    /**
     * Returns a single study by ID.
     *
     * @param id the study ID
     * @throws StudyNotFoundException if no study exists with this ID
     */
    @Transactional(readOnly = true)
    public StudyResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    // ── Command methods ───────────────────────────────────────────────────────

    /**
     * Creates a new study in DRAFT status.
     * Status is set by {@code Study}'s {@code @Builder.Default} — not by this method.
     *
     * @param req validated inbound payload
     */
    @Transactional
    public StudyResponse create(StudyRequest req) {
        Study study = Study.builder()
            .title(req.getTitle())
            .maxEnrollment(req.getMaxEnrollment())
            .eligibilityCriteria(req.getEligibilityCriteria())
            .build();
        return toResponse(studyRepository.save(study));
    }

    /**
     * Updates mutable fields of an existing study.
     * Status changes must go through {@link #transitionStatus} — not here.
     *
     * @param id  the study ID to update
     * @param req validated inbound payload
     * @throws StudyNotFoundException if no study exists with this ID
     */
    @Transactional
    public StudyResponse update(Long id, StudyRequest req) {
        Study study = getOrThrow(id);
        study.setTitle(req.getTitle());
        study.setMaxEnrollment(req.getMaxEnrollment());
        study.setEligibilityCriteria(req.getEligibilityCriteria());
        return toResponse(studyRepository.save(study));
    }

    /**
     * Advances the study to the next lifecycle state.
     *
     * <p>Delegates the transition validity check to {@link StudyStatus#isValidTransition},
     * which is the single source of truth for allowed moves.
     *
     * @param id        the study ID
     * @param newStatus the requested target state
     * @throws StudyNotFoundException           if no study exists with this ID
     * @throws InvalidStateTransitionException  if the transition is not legal
     */
    @Transactional
    public StudyResponse transitionStatus(Long id, StudyStatus newStatus) {
        Study study = getOrThrow(id);

        if (!study.getStatus().isValidTransition(newStatus)) {
            throw new InvalidStateTransitionException(
                study.getStatus().name(), newStatus.name()
            );
        }

        study.setStatus(newStatus);
        return toResponse(studyRepository.save(study));
    }

    /**
     * Deletes a study.
     *
     * <p>OPEN studies cannot be deleted because they have enrolled patients.
     * They must be CLOSED first via {@link #transitionStatus}.
     *
     * @param id the study ID
     * @throws StudyNotFoundException              if no study exists with this ID
     * @throws StudyDeletionNotAllowedException    if the study is currently OPEN
     */
    @Transactional
    public void delete(Long id) {
        Study study = getOrThrow(id);

        if (study.getStatus() == StudyStatus.OPEN) {
            throw new StudyDeletionNotAllowedException(id);
        }

        studyRepository.delete(study);
    }

    // ── Package-private helpers (used by RecruitmentService) ─────────────────

    /**
     * Fetches a study by ID or throws a typed 404.
     * Package-private so {@code RecruitmentService} can reuse it.
     *
     * @param id the study ID
     * @throws StudyNotFoundException if not found
     */
    public Study getOrThrow(Long id) {
        return studyRepository.findById(id)
            .orElseThrow(() -> new StudyNotFoundException(id));
    }

    /**
     * Maps a {@link Study} entity to its outbound {@link StudyResponse} DTO.
     *
     * @param s the Study entity
     * @return the corresponding DTO
     */
    public StudyResponse toResponse(Study s) {
        return new StudyResponse(
            s.getId(),
            s.getTitle(),
            s.getStatus(),
            s.getMaxEnrollment(),
            s.getCurrentEnrollment(),
            s.getEligibilityCriteria(),
            s.getLastRecruitedAt()
        );
    }
}
