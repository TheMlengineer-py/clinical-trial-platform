package com.bci.trial.service;

import com.bci.trial.domain.*;
import com.bci.trial.dto.*;
import com.bci.trial.exception.*;
import com.bci.trial.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

/**
 * Service layer for patient management.
 *
 * <p>Delete operations are soft — {@code deletedAt} is stamped rather than
 * issuing a SQL DELETE, preserving recruitment history and audit trail.
 *
 * <p>Eligibility evaluation has been extracted to {@link EligibilityEngine}
 * for testability and to support advanced rule combinations.
 */
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    @Transactional(readOnly = true)
    public Page<PatientResponse> findAll(String condition, Pageable pageable) {
        Page<Patient> page = (condition != null && !condition.isBlank())
            ? patientRepository.findByConditionContainingIgnoreCase(condition, pageable)
            : patientRepository.findAllSortedByRecruitedAt(pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PatientResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public PatientResponse create(PatientRequest req) {
        Patient patient = Patient.builder()
            .name(req.getName())
            .age(req.getAge())
            .condition(req.getCondition())
            .build();
        return toResponse(patientRepository.save(patient));
    }

    @Transactional
    public PatientResponse update(Long id, PatientRequest req) {
        Patient patient = getOrThrow(id);
        patient.setName(req.getName());
        patient.setAge(req.getAge());
        patient.setCondition(req.getCondition());
        return toResponse(patientRepository.save(patient));
    }

    /**
     * Soft-deletes a patient by stamping {@code deletedAt}.
     * Recruitment history is preserved in the database.
     */
    @Transactional
    public void delete(Long id) {
        Patient patient = getOrThrow(id);
        patient.setDeletedAt(Instant.now());
        patientRepository.save(patient);
    }

    public Patient getOrThrow(Long id) {
        return patientRepository.findById(id)
            .orElseThrow(() -> new PatientNotFoundException(id));
    }

    public PatientResponse toResponse(Patient p) {
        return new PatientResponse(
            p.getId(),
            p.getName(),
            p.getAge(),
            p.getCondition(),
            p.getEnrolledStudyId(),
            p.getRecruitedAt()
        );
    }
}
