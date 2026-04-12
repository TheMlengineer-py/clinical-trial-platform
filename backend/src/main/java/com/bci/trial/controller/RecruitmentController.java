package com.bci.trial.controller;

import com.bci.trial.dto.*;
import com.bci.trial.service.RecruitmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recruitment")
@RequiredArgsConstructor
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    @PreAuthorize("hasAnyRole('ADMIN', 'RESEARCHER')")
    @PostMapping
    public ResponseEntity<PatientResponse> recruit(
            @Valid @RequestBody RecruitmentRequest req) {
        PatientResponse response = recruitmentService.recruit(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
