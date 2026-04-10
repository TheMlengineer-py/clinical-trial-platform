package com.bci.trial.controller;

import com.bci.trial.dto.*;
import com.bci.trial.exception.*;
import com.bci.trial.service.RecruitmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link RecruitmentController}.
 * Covers all HTTP response codes the assessment requires.
 */
@WebMvcTest(RecruitmentController.class)
class RecruitmentControllerTest {

    @Autowired MockMvc          mockMvc;
    @Autowired ObjectMapper     mapper;
    @MockBean  RecruitmentService recruitmentService;

    private RecruitmentRequest validReq;

    @BeforeEach
    void setUp() {
        validReq = new RecruitmentRequest();
        validReq.setPatientId(1L);
        validReq.setStudyId(2L);
    }

    @Test
    @DisplayName("POST /api/recruitment returns 201 on success")
    void recruit_success_returns201() throws Exception {
        PatientResponse res = new PatientResponse(1L, "Alice", 30, "NSCLC", 2L, null);
        when(recruitmentService.recruit(any())).thenReturn(res);

        mockMvc.perform(post("/api/recruitment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.enrolledStudyId").value(2));
    }

    @Test
    @DisplayName("POST /api/recruitment returns 409 when study is not OPEN")
    void recruit_studyNotOpen_returns409() throws Exception {
        when(recruitmentService.recruit(any())).thenThrow(new StudyNotOpenException(2L));

        mockMvc.perform(post("/api/recruitment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validReq)))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/recruitment returns 409 when study is at capacity")
    void recruit_capacityExceeded_returns409() throws Exception {
        when(recruitmentService.recruit(any())).thenThrow(new EnrollmentCapacityException(2L));

        mockMvc.perform(post("/api/recruitment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validReq)))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/recruitment returns 422 when patient is ineligible")
    void recruit_ineligible_returns422() throws Exception {
        when(recruitmentService.recruit(any())).thenThrow(new EligibilityException(1L, 2L));

        mockMvc.perform(post("/api/recruitment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validReq)))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /api/recruitment returns 400 when patientId is missing")
    void recruit_missingPatientId_returns400() throws Exception {
        RecruitmentRequest bad = new RecruitmentRequest();
        bad.setStudyId(2L); // no patientId

        mockMvc.perform(post("/api/recruitment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(bad)))
            .andExpect(status().isBadRequest());
    }
}
