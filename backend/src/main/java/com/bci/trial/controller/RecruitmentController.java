package com.bci.trial.controller;

import com.bci.trial.dto.*;
import com.bci.trial.service.RecruitmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the patient recruitment operation.
 *
 * <p>Base path: {@code /api/recruitment}
 *
 * <p>A single POST endpoint because recruitment is an action (a command),
 * not a resource — it deserves its own dedicated endpoint and controller
 * rather than being buried inside the patient or study controllers.
 *
 * <p>All business rules (OPEN-only, capacity, eligibility, concurrency)
 * are enforced in {@code RecruitmentService} — the controller is intentionally
 * thin: validate, delegate, respond.
 */
@RestController
@RequestMapping("/api/recruitment")
@RequiredArgsConstructor
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    /**
     * Recruits a patient into a study.
     *
     * <p>Returns 201 Created with the updated patient payload on success.
     * Returns structured 4xx errors on any business rule violation:
     * <ul>
     *   <li>404 — patient or study not found</li>
     *   <li>409 — study not open, patient already enrolled, capacity exceeded</li>
     *   <li>422 — patient does not meet eligibility criteria</li>
     * </ul>
     *
     * @param req validated recruitment request body (patientId + studyId)
     * @return 201 with updated patient response
     */
    @PostMapping
    public ResponseEntity<PatientResponse> recruit(@Valid @RequestBody RecruitmentRequest req) {
        PatientResponse response = recruitmentService.recruit(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
