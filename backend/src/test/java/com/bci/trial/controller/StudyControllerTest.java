package com.bci.trial.controller;

import com.bci.trial.config.SecurityConfig;
import com.bci.trial.config.UserConfig;
import com.bci.trial.domain.StudyStatus;
import com.bci.trial.dto.*;
import com.bci.trial.exception.*;
import com.bci.trial.service.StudyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudyController.class)
@Import({SecurityConfig.class, UserConfig.class})
@WithMockUser(roles = "ADMIN")
class StudyControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper mapper;
    @MockBean  StudyService studyService;

    private StudyResponse sampleStudy;

    @BeforeEach
    void setUp() {
        sampleStudy = new StudyResponse(
            1L, "BRCA Trial", StudyStatus.OPEN, 25, 10, "age>18", null);
    }

    @Test @DisplayName("GET /api/studies returns 200 for admin")
    void list_admin_returns200() throws Exception {
        when(studyService.findAll(any(), any()))
            .thenReturn(new PageImpl<>(List.of(sampleStudy)));
        mockMvc.perform(get("/api/studies"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].title").value("BRCA Trial"));
    }

    @Test @DisplayName("GET /api/studies returns 200 for researcher")
    @WithMockUser(roles = "RESEARCHER")
    void list_researcher_returns200() throws Exception {
        when(studyService.findAll(any(), any()))
            .thenReturn(new PageImpl<>(List.of(sampleStudy)));
        mockMvc.perform(get("/api/studies"))
            .andExpect(status().isOk());
    }

    @Test @DisplayName("POST /api/studies returns 201 for admin")
    void create_admin_returns201() throws Exception {
        StudyRequest req = new StudyRequest();
        req.setTitle("New Study");
        req.setMaxEnrollment(20);
        when(studyService.create(any())).thenReturn(sampleStudy);
        mockMvc.perform(post("/api/studies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated());
    }

    @Test @DisplayName("POST /api/studies returns 403 for researcher")
    @WithMockUser(roles = "RESEARCHER")
    void create_researcher_returns403() throws Exception {
        StudyRequest req = new StudyRequest();
        req.setTitle("New Study");
        req.setMaxEnrollment(20);
        mockMvc.perform(post("/api/studies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isForbidden());
    }

    @Test @DisplayName("POST /api/studies returns 400 for missing title")
    void create_missingTitle_returns400() throws Exception {
        StudyRequest req = new StudyRequest();
        req.setMaxEnrollment(5);
        mockMvc.perform(post("/api/studies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test @DisplayName("PATCH /api/studies/1/status returns 409 for invalid transition")
    void transition_invalid_returns409() throws Exception {
        when(studyService.transitionStatus(1L, StudyStatus.ARCHIVED))
            .thenThrow(new InvalidStateTransitionException("OPEN", "ARCHIVED"));
        mockMvc.perform(patch("/api/studies/1/status")
                .param("status", "ARCHIVED"))
            .andExpect(status().isConflict());
    }

    @Test @DisplayName("PATCH /api/studies/1/status returns 403 for researcher")
    @WithMockUser(roles = "RESEARCHER")
    void transition_researcher_returns403() throws Exception {
        mockMvc.perform(patch("/api/studies/1/status")
                .param("status", "OPEN"))
            .andExpect(status().isForbidden());
    }

    @Test @DisplayName("DELETE /api/studies/1 returns 409 when OPEN")
    void delete_openStudy_returns409() throws Exception {
        doThrow(new StudyDeletionNotAllowedException(1L))
            .when(studyService).delete(1L);
        mockMvc.perform(delete("/api/studies/1"))
            .andExpect(status().isConflict());
    }

    @Test @DisplayName("DELETE /api/studies/1 returns 403 for researcher")
    @WithMockUser(roles = "RESEARCHER")
    void delete_researcher_returns403() throws Exception {
        mockMvc.perform(delete("/api/studies/1"))
            .andExpect(status().isForbidden());
    }
}
