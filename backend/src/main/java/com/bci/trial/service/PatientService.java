package com.bci.trial.service;

import com.bci.trial.domain.*;
import com.bci.trial.dto.*;
import com.bci.trial.exception.*;
import com.bci.trial.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for patient management and eligibility checking.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>CRUD operations for patients</li>
 *   <li>Eligibility rule parsing and evaluation</li>
 *   <li>Pagination and condition filtering</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    // ── Query methods ─────────────────────────────────────────────────────────

    /**
     * Returns a paginated list of patients, optionally filtered by condition.
     * Default sort is by most recently recruited (handled in the repository query).
     *
     * @param condition partial condition string filter; {@code null} returns all patients
     * @param pageable  pagination parameters
     */
    @Transactional(readOnly = true)
    public Page<PatientResponse> findAll(String condition, Pageable pageable) {
        Page<Patient> page = (condition != null && !condition.isBlank())
            ? patientRepository.findByConditionContainingIgnoreCase(condition, pageable)
            : patientRepository.findAllSortedByRecruitedAt(pageable);
        return page.map(this::toResponse);
    }

    /**
     * Returns a single patient by ID.
     *
     * @param id the patient ID
     * @throws PatientNotFoundException if no patient exists with this ID
     */
    @Transactional(readOnly = true)
    public PatientResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    // ── Command methods ───────────────────────────────────────────────────────

    /**
     * Creates a new patient with no active enrolment.
     *
     * @param req validated inbound payload
     */
    @Transactional
    public PatientResponse create(PatientRequest req) {
        Patient patient = Patient.builder()
            .name(req.getName())
            .age(req.getAge())
            .condition(req.getCondition())
            .build();
        return toResponse(patientRepository.save(patient));
    }

    /**
     * Updates mutable fields of an existing patient.
     * Enrolment state is managed by {@code RecruitmentService} — not here.
     *
     * @param id  the patient ID
     * @param req validated inbound payload
     * @throws PatientNotFoundException if no patient exists with this ID
     */
    @Transactional
    public PatientResponse update(Long id, PatientRequest req) {
        Patient patient = getOrThrow(id);
        patient.setName(req.getName());
        patient.setAge(req.getAge());
        patient.setCondition(req.getCondition());
        return toResponse(patientRepository.save(patient));
    }

    /**
     * Deletes a patient record.
     *
     * @param id the patient ID
     * @throws PatientNotFoundException if no patient exists with this ID
     */
    @Transactional
    public void delete(Long id) {
        Patient patient = getOrThrow(id);
        patientRepository.delete(patient);
    }

    // ── Eligibility ───────────────────────────────────────────────────────────

    /**
     * Evaluates whether a patient satisfies a study's eligibility criteria string.
     *
     * <p>Criteria format: comma-separated rules, each of the form:
     * {@code field operator value}
     * <ul>
     *   <li>Supported fields: {@code age}, {@code condition}</li>
     *   <li>Supported operators: {@code >}, {@code <}, {@code =}</li>
     * </ul>
     * Example: {@code "age>18,condition=NSCLC"}
     *
     * <p>An empty or null criteria string passes all patients (no restriction).
     *
     * @param patient  the patient to evaluate
     * @param criteria the study's eligibilityCriteria string
     * @return {@code true} if the patient meets all rules
     */
    public boolean meetsEligibility(Patient patient, String criteria) {
        if (criteria == null || criteria.isBlank()) return true;

        for (String rule : criteria.split(",")) {
            rule = rule.trim();

            // Parse age rules: age>18, age<65
            if (rule.startsWith("age")) {
                char op = rule.charAt(3);
                int threshold = Integer.parseInt(rule.substring(4).trim());
                boolean passes = switch (op) {
                    case '>' -> patient.getAge() > threshold;
                    case '<' -> patient.getAge() < threshold;
                    case '=' -> patient.getAge() == threshold;
                    default  -> true; // unknown operator — pass by default
                };
                if (!passes) return false;
            }

            // Parse condition rules: condition=NSCLC (case-insensitive)
            else if (rule.startsWith("condition=")) {
                String required = rule.substring("condition=".length()).trim();
                if (!patient.getCondition().equalsIgnoreCase(required)) return false;
            }
        }

        return true;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Fetches a patient by ID or throws a typed 404.
     * Package-accessible so {@code RecruitmentService} can reuse it.
     *
     * @param id the patient ID
     * @throws PatientNotFoundException if not found
     */
    public Patient getOrThrow(Long id) {
        return patientRepository.findById(id)
            .orElseThrow(() -> new PatientNotFoundException(id));
    }

    /**
     * Maps a {@link Patient} entity to its outbound {@link PatientResponse} DTO.
     *
     * @param p the Patient entity
     * @return the corresponding DTO
     */
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
