package com.bci.trial.controller;

import com.bci.trial.domain.StudyStatus;
import com.bci.trial.dto.*;
import com.bci.trial.service.StudyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    @GetMapping
    public ResponseEntity<Page<StudyResponse>> list(
        @RequestParam(required = false) StudyStatus status,
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "lastRecruitedAt,desc") String sort
    ) {
        String[] parts     = sort.split(",");
        Sort.Direction dir = parts.length > 1 && parts[1].equalsIgnoreCase("asc")
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable  = PageRequest.of(page, size, Sort.by(dir, parts[0]));
        return ResponseEntity.ok(studyService.findAll(status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(studyService.findById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<StudyResponse> create(@Valid @RequestBody StudyRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studyService.create(req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<StudyResponse> update(
        @PathVariable Long id, @Valid @RequestBody StudyRequest req) {
        return ResponseEntity.ok(studyService.update(id, req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<StudyResponse> transition(
        @PathVariable Long id, @RequestParam StudyStatus status) {
        return ResponseEntity.ok(studyService.transitionStatus(id, status));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
