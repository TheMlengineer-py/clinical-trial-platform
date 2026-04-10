package com.bci.trial.controller;

import com.bci.trial.domain.StudyStatus;
import com.bci.trial.dto.*;
import com.bci.trial.service.StudyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for study management.
 *
 * <p>Base path: {@code /api/studies}
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET    /api/studies}          — paginated list with optional status filter</li>
 *   <li>{@code GET    /api/studies/{id}}      — single study</li>
 *   <li>{@code POST   /api/studies}           — create study (DRAFT)</li>
 *   <li>{@code PUT    /api/studies/{id}}      — update mutable fields</li>
 *   <li>{@code PATCH  /api/studies/{id}/status} — advance lifecycle state</li>
 *   <li>{@code DELETE /api/studies/{id}}      — delete (blocked if OPEN)</li>
 * </ul>
 *
 * <p>All validation is handled by {@code @Valid} — failures are caught by
 * {@code GlobalExceptionHandler} and returned as structured JSON errors.
 */
@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    /**
     * Returns a paginated list of studies.
     *
     * @param status optional status filter (DRAFT, OPEN, CLOSED, ARCHIVED)
     * @param page   zero-based page index (default 0)
     * @param size   page size (default 10)
     * @param sort   sort field and direction, e.g. "lastRecruitedAt,desc"
     */
    @GetMapping
    public ResponseEntity<Page<StudyResponse>> list(
        @RequestParam(required = false) StudyStatus status,
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "lastRecruitedAt,desc") String sort
    ) {
        String[] parts    = sort.split(",");
        Sort.Direction dir = parts.length > 1 && parts[1].equalsIgnoreCase("asc")
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, parts[0]));
        return ResponseEntity.ok(studyService.findAll(status, pageable));
    }

    /**
     * Returns a single study by ID.
     *
     * @param id the study ID (path variable)
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(studyService.findById(id));
    }

    /**
     * Creates a new study in DRAFT status.
     * Returns 201 Created with the created resource in the body.
     *
     * @param req validated request body
     */
    @PostMapping
    public ResponseEntity<StudyResponse> create(@Valid @RequestBody StudyRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studyService.create(req));
    }

    /**
     * Updates mutable fields of an existing study (title, maxEnrollment, eligibilityCriteria).
     *
     * @param id  the study ID (path variable)
     * @param req validated request body
     */
    @PutMapping("/{id}")
    public ResponseEntity<StudyResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody StudyRequest req
    ) {
        return ResponseEntity.ok(studyService.update(id, req));
    }

    /**
     * Advances the study to the next lifecycle state.
     * Uses PATCH because only the status field changes.
     * Delegates transition validation to {@code StudyService.transitionStatus()}.
     *
     * @param id     the study ID (path variable)
     * @param status the target state as a query parameter, e.g. ?status=OPEN
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<StudyResponse> transition(
        @PathVariable Long id,
        @RequestParam StudyStatus status
    ) {
        return ResponseEntity.ok(studyService.transitionStatus(id, status));
    }

    /**
     * Deletes a study. Will throw 409 if the study is OPEN.
     *
     * @param id the study ID (path variable)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
