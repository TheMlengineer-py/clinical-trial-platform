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
     *
     * @param id the patient ID
     * @throws PatientNotFoundException if no active patient exists with this ID
     */
    @Transactional
    public void delete(Long id) {
        Patient patient = getOrThrow(id);
        patient.setDeletedAt(Instant.now());
        patientRepository.save(patient);
    }

    public boolean meetsEligibility(Patient patient, String criteria) {
        if (criteria == null || criteria.isBlank()) return true;

        for (String rule : criteria.split(",")) {
            rule = rule.trim();

            if (rule.startsWith("age")) {
                char op = rule.charAt(3);
                int threshold = Integer.parseInt(rule.substring(4).trim());
                boolean passes = switch (op) {
                    case '>' -> patient.getAge() > threshold;
                    case '<' -> patient.getAge() < threshold;
                    case '=' -> patient.getAge() == threshold;
                    default  -> true;
                };
                if (!passes) return false;
            }

            else if (rule.startsWith("condition=")) {
                String required = rule.substring("condition=".length()).trim();
                if (!patient.getCondition().equalsIgnoreCase(required)) return false;
            }
        }

        return true;
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
