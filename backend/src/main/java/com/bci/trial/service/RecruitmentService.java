package com.bci.trial.service;

import com.bci.trial.domain.*;
import com.bci.trial.dto.*;
import com.bci.trial.event.*;
import com.bci.trial.exception.*;
import com.bci.trial.repository.StudyRepository;
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
 *   <li>Acquire a <strong>pessimistic write lock</strong> on the target study row</li>
 *   <li>Re-validate that the study is OPEN (state may have changed before lock)</li>
 *   <li>Re-validate that capacity is not exceeded (concurrent enrolments may have filled it)</li>
 *   <li>Check patient eligibility against the study criteria</li>
 *   <li>Increment enrollment count and set enrollment on the patient</li>
 *   <li>Publish a {@link PatientRecruitedEvent} for downstream handlers</li>
 * </ol>
 *
 * <p><strong>Why pessimistic locking?</strong><br>
 * In the last-available-slot scenario, two concurrent requests can both pass the
 * capacity check before either writes. The {@code SELECT FOR UPDATE} lock ensures
 * only one transaction proceeds at a time at the critical section, and the loser
 * sees the updated count and throws {@link EnrollmentCapacityException}.
 */
@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final StudyRepository   studyRepository;
    private final PatientService    patientService;
    private final DomainEventPublisher eventPublisher;

    /**
     * Recruits a patient into a study.
     *
     * <p>The entire method runs in a single SERIALIZABLE-equivalent transaction
     * due to the pessimistic lock held on the study row from step 3 until commit.
     *
     * @param req the recruitment request containing patientId and studyId
     * @return the updated patient response after enrolment
     *
     * @throws PatientNotFoundException         if patientId does not exist
     * @throws StudyNotFoundException           if studyId does not exist
     * @throws PatientAlreadyEnrolledException  if patient is already enrolled elsewhere
     * @throws StudyNotOpenException            if study is not in OPEN status
     * @throws EnrollmentCapacityException      if study has reached maxEnrollment
     * @throws EligibilityException             if patient does not meet study criteria
     */
    @Transactional
    public PatientResponse recruit(RecruitmentRequest req) {
        Instant now = Instant.now();

        // Step 1 — Resolve patient (throws 404 if absent)
        Patient patient = patientService.getOrThrow(req.getPatientId());

        // Step 2 — Fast pre-check: is the patient already enrolled?
        // This check runs before acquiring the expensive lock to fail fast
        // on the common case where a researcher tries to double-enrol.
        if (patient.getEnrolledStudyId() != null) {
            throw new PatientAlreadyEnrolledException(req.getPatientId());
        }

        // Step 3 — Acquire pessimistic write lock on the study row.
        // All other recruitment requests targeting this study will block here
        // until our transaction commits or rolls back.
        Study study = studyRepository.findByIdForUpdate(req.getStudyId())
            .orElseThrow(() -> new StudyNotFoundException(req.getStudyId()));

        // Step 4 — Re-check status under the lock.
        // The study could have been CLOSED between our pre-check and lock acquisition.
        if (study.getStatus() != StudyStatus.OPEN) {
            throw new StudyNotOpenException(req.getStudyId());
        }

        // Step 5 — Re-check capacity under the lock.
        // Critical: another thread may have filled the last slot while we waited for the lock.
        if (study.getCurrentEnrollment() >= study.getMaxEnrollment()) {
            throw new EnrollmentCapacityException(req.getStudyId());
        }

        // Step 6 — Eligibility check.
        if (!patientService.meetsEligibility(patient, study.getEligibilityCriteria())) {
            throw new EligibilityException(req.getPatientId(), req.getStudyId());
        }

        // Step 7 — All guards passed. Perform the enrollment.
        study.setCurrentEnrollment(study.getCurrentEnrollment() + 1);
        study.setLastRecruitedAt(now);
        studyRepository.save(study);

        patient.setEnrolledStudyId(study.getId());
        patient.setRecruitedAt(now);
        // Save via the repository directly since PatientService.update() would
        // reset other fields from a DTO we don't have here.
        Patient saved = patientService.getOrThrow(patient.getId());
        saved.setEnrolledStudyId(study.getId());
        saved.setRecruitedAt(now);

        // Step 8 — Publish domain event (handled by DomainEventPublisher listener).
        // Event is published inside the transaction — if the transaction rolls back,
        // the event handler should be idempotent or use @TransactionalEventListener.
        eventPublisher.publish(new PatientRecruitedEvent(
            patient.getId(), study.getId(), now
        ));

        return patientService.toResponse(saved);
    }
}
