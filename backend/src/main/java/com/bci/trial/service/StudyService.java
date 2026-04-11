package com.bci.trial.service;

import com.bci.trial.domain.*;
import com.bci.trial.dto.*;
import com.bci.trial.exception.*;
import com.bci.trial.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

/**
 * Service layer for study management.
 *
 * <p>Delete operations are soft — {@code deletedAt} is stamped rather than
 * issuing a SQL DELETE, preserving audit history and allowing recovery.
 */
@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;

    @Transactional(readOnly = true)
    public Page<StudyResponse> findAll(StudyStatus status, Pageable pageable) {
        Page<Study> page = (status != null)
            ? studyRepository.findByStatus(status, pageable)
            : studyRepository.findAllActive(pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public StudyResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public StudyResponse create(StudyRequest req) {
        Study study = Study.builder()
            .title(req.getTitle())
            .maxEnrollment(req.getMaxEnrollment())
            .eligibilityCriteria(req.getEligibilityCriteria())
            .build();
        return toResponse(studyRepository.save(study));
    }

    @Transactional
    public StudyResponse update(Long id, StudyRequest req) {
        Study study = getOrThrow(id);
        study.setTitle(req.getTitle());
        study.setMaxEnrollment(req.getMaxEnrollment());
        study.setEligibilityCriteria(req.getEligibilityCriteria());
        return toResponse(studyRepository.save(study));
    }

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
     * Soft-deletes a study by stamping {@code deletedAt}.
     * OPEN studies cannot be deleted — they must be CLOSED first.
     *
     * @param id the study ID
     * @throws StudyNotFoundException           if no active study exists with this ID
     * @throws StudyDeletionNotAllowedException if the study is currently OPEN
     */
    @Transactional
    public void delete(Long id) {
        Study study = getOrThrow(id);

        if (study.getStatus() == StudyStatus.OPEN) {
            throw new StudyDeletionNotAllowedException(id);
        }

        study.setDeletedAt(Instant.now());
        studyRepository.save(study);
    }

    public Study getOrThrow(Long id) {
        return studyRepository.findActiveById(id)
            .orElseThrow(() -> new StudyNotFoundException(id));
    }

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
