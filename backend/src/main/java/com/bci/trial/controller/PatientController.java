package com.bci.trial.controller;

import com.bci.trial.dto.*;
import com.bci.trial.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for patient management.
 *
 * <p>Base path: {@code /api/patients}
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET    /api/patients}       — paginated list with optional condition filter</li>
 *   <li>{@code GET    /api/patients/{id}}   — single patient with enrolment detail</li>
 *   <li>{@code POST   /api/patients}        — create patient</li>
 *   <li>{@code PUT    /api/patients/{id}}   — update patient fields</li>
 *   <li>{@code DELETE /api/patients/{id}}   — delete patient</li>
 * </ul>
 *
 * <p>Enrolment is managed exclusively through {@code POST /api/recruitment} —
 * not through the patient endpoints. This prevents partial-state writes
 * where enrolledStudyId is set but the study count is not incremented.
 */
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    /**
     * Returns a paginated list of patients.
     * Default sort is most recently recruited (DESC).
     *
     * @param condition optional partial condition filter (case-insensitive)
     * @param page      zero-based page index (default 0)
     * @param size      page size (default 10)
     */
    @GetMapping
    public ResponseEntity<Page<PatientResponse>> list(
        @RequestParam(required = false) String condition,
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        // Sort is enforced in the repository JPQL query (recruitedAt DESC NULLS LAST)
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(patientService.findAll(condition, pageable));
    }

    /**
     * Returns a single patient by ID, including current enrolment details.
     *
     * @param id the patient ID (path variable)
     */
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.findById(id));
    }

    /**
     * Creates a new patient with no active enrolment.
     * Returns 201 Created.
     *
     * @param req validated request body
     */
    @PostMapping
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody PatientRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.create(req));
    }

    /**
     * Updates mutable fields of an existing patient (name, age, condition).
     * Enrolment state cannot be changed via this endpoint.
     *
     * @param id  the patient ID (path variable)
     * @param req validated request body
     */
    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody PatientRequest req
    ) {
        return ResponseEntity.ok(patientService.update(id, req));
    }

    /**
     * Deletes a patient record.
     *
     * @param id the patient ID (path variable)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        patientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
