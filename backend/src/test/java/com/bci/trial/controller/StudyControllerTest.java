package com.bci.trial.controller;

import com.bci.trial.domain.StudyStatus;
import com.bci.trial.dto.*;
import com.bci.trial.exception.*;
import com.bci.trial.service.StudyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link StudyController} using MockMvc.
 *
 * <p>{@code @WebMvcTest} loads only the web layer — no database, no full context.
 * The service is mocked, so these tests verify the HTTP contract:
 * correct status codes, request validation, and JSON serialisation.
 */
@WebMvcTest(StudyController.class)
class StudyControllerTest {

    @Autowired MockMvc     mockMvc;
    @Autowired ObjectMapper mapper;
    @MockBean  StudyService studyService;

    private StudyResponse sampleStudy;

    @BeforeEach
    void setUp() {
        sampleStudy = new StudyResponse(1L, "BRCA Trial", StudyStatus.OPEN, 25, 10, "age>18", null);
    }

    // ── GET /api/studies ──────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/studies returns 200 with paginated list")
    void list_returns200() throws Exception {
        when(studyService.findAll(null, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lastRecruitedAt"))))
            .thenReturn(new PageImpl<>(List.of(sampleStudy)));

        mockMvc.perform(get("/api/studies"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].title").value("BRCA Trial"));
    }

    // ── POST /api/studies ─────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/studies returns 201 for valid request")
    void create_validRequest_returns201() throws Exception {
        StudyRequest req = new StudyRequest();
        req.setTitle("New Study");
        req.setMaxEnrollment(20);

        when(studyService.create(any())).thenReturn(sampleStudy);

        mockMvc.perform(post("/api/studies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/studies returns 400 for missing title")
    void create_missingTitle_returns400() throws Exception {
        StudyRequest req = new StudyRequest();
        req.setMaxEnrollment(5); // no title

        mockMvc.perform(post("/api/studies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    // ── PATCH /api/studies/{id}/status ────────────────────────────────────────

    @Test
    @DisplayName("PATCH /api/studies/1/status returns 409 for invalid transition")
    void transition_invalidState_returns409() throws Exception {
        when(studyService.transitionStatus(1L, StudyStatus.ARCHIVED))
            .thenThrow(new InvalidStateTransitionException("OPEN", "ARCHIVED"));

        mockMvc.perform(patch("/api/studies/1/status").param("status", "ARCHIVED"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").exists());
    }

    // ── DELETE /api/studies/{id} ──────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/studies/1 returns 409 when study is OPEN")
    void delete_openStudy_returns409() throws Exception {
        doThrow(new StudyDeletionNotAllowedException(1L)).when(studyService).delete(1L);

        mockMvc.perform(delete("/api/studies/1"))
            .andExpect(status().isConflict());
    }
}
