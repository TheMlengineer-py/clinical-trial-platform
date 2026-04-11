package com.bci.trial.service;

import com.bci.trial.domain.*;
import com.bci.trial.dto.*;
import com.bci.trial.event.*;
import com.bci.trial.exception.*;
import com.bci.trial.repository.StudyRepository;
import com.bci.trial.service.EligibilityEngine.EligibilityResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

/**
 * Service handling the patient recruitment transaction.
 *
 * <p>This is the most critical and concurrency-sensitive operation in the system.
 * The recruitment flow executes the following steps inside a single transaction:
 *
 * <ol>
 *   <li>Validate that the patient exists</li>
 *   <li>Validate that the patient is not already enrolled (fast pre-check)</li>
 *   <li>Acquire a pessimistic write lock on the target study row</li>
 *   <li>Re-validate that the study is OPEN (state may have changed before lock)</li>
 *   <li>Re-validate that capacity is not exceeded</li>
 *   <li>Evaluate patient eligibility via {@link EligibilityEngine}</li>
 *   <li>Increment enrollment count and set enrollment on the patient</li>
 *   <li>Publish a {@link PatientRecruitedEvent} for downstream handlers</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final StudyRepository      studyRepository;
    private final PatientService       patientService;
    private final EligibilityEngine    eligibilityEngine;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public PatientResponse recruit(RecruitmentRequest req) {
        Instant now = Instant.now();

        // Step 1 — Resolve patient
        Patient patient = patientService.getOrThrow(req.getPatientId());

        // Step 2 — Fast pre-check: already enrolled?
        if (patient.getEnrolledStudyId() != null) {
            throw new PatientAlreadyEnrolledException(req.getPatientId());
        }

        // Step 3 — Acquire pessimistic write lock on study row
        Study study = studyRepository.findByIdForUpdate(req.getStudyId())
            .orElseThrow(() -> new StudyNotFoundException(req.getStudyId()));

        // Step 4 — Re-check status under lock
        if (study.getStatus() != StudyStatus.OPEN) {
            throw new StudyNotOpenException(req.getStudyId());
        }

        // Step 5 — Re-check capacity under lock
        if (study.getCurrentEnrollment() >= study.getMaxEnrollment()) {
            throw new EnrollmentCapacityException(req.getStudyId());
        }

        // Step 6 — Advanced eligibility engine evaluation
        EligibilityResult result = eligibilityEngine.evaluate(
            patient, study.getEligibilityCriteria()
        );
        if (!result.eligible()) {
            throw new EligibilityException(
                req.getPatientId(), req.getStudyId(), result.reason()
            );
        }

        // Step 7 — All guards passed. Perform the enrollment.
        study.setCurrentEnrollment(study.getCurrentEnrollment() + 1);
        study.setLastRecruitedAt(now);
        studyRepository.save(study);

        Patient saved = patientService.getOrThrow(patient.getId());
        saved.setEnrolledStudyId(study.getId());
        saved.setRecruitedAt(now);

        // Step 8 — Publish domain event
        eventPublisher.publish(new PatientRecruitedEvent(
            patient.getId(), study.getId(), now
        ));

        return patientService.toResponse(saved);
    }
}
