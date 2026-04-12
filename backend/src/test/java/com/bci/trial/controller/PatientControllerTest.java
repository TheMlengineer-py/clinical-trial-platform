package com.bci.trial.controller;

import com.bci.trial.config.SecurityConfig;
import com.bci.trial.config.UserConfig;
import com.bci.trial.dto.PatientRequest;
import com.bci.trial.dto.PatientResponse;
import com.bci.trial.exception.PatientNotFoundException;
import com.bci.trial.service.PatientService;
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

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
@Import({SecurityConfig.class, UserConfig.class})
@WithMockUser(roles = "ADMIN")
class PatientControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper mapper;
    @MockBean  PatientService patientService;

    private PatientResponse sampleResponse;
    private PatientRequest  validRequest;

    @BeforeEach
    void setUp() {
        sampleResponse = new PatientResponse(
            1L, "Alice Nwosu", 42, "Breast cancer", null, Instant.now());
        validRequest = new PatientRequest();
        validRequest.setName("Alice Nwosu");
        validRequest.setAge(42);
        validRequest.setCondition("Breast cancer");
    }

    @Test @DisplayName("GET /api/patients returns 200")
    void list_returns200() throws Exception {
        when(patientService.findAll(any(), any()))
            .thenReturn(new PageImpl<>(List.of(sampleResponse)));
        mockMvc.perform(get("/api/patients"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Alice Nwosu"));
    }

    @Test @DisplayName("GET /api/patients/{id} returns 200 for known ID")
    void getById_known_returns200() throws Exception {
        when(patientService.findById(1L)).thenReturn(sampleResponse);
        mockMvc.perform(get("/api/patients/1"))
            .andExpect(status().isOk());
    }

    @Test @DisplayName("GET /api/patients/{id} returns 404 for unknown ID")
    void getById_unknown_returns404() throws Exception {
        when(patientService.findById(99L))
            .thenThrow(new PatientNotFoundException(99L));
        mockMvc.perform(get("/api/patients/99"))
            .andExpect(status().isNotFound());
    }

    @Test @DisplayName("POST /api/patients returns 201 for admin")
    void create_admin_returns201() throws Exception {
        when(patientService.create(any())).thenReturn(sampleResponse);
        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validRequest)))
            .andExpect(status().isCreated());
    }

    @Test @DisplayName("POST /api/patients returns 403 for researcher")
    @WithMockUser(roles = "RESEARCHER")
    void create_researcher_returns403() throws Exception {
        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validRequest)))
            .andExpect(status().isForbidden());
    }

    @Test @DisplayName("POST /api/patients returns 400 for blank name")
    void create_blankName_returns400() throws Exception {
        validRequest.setName("");
        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test @DisplayName("PUT /api/patients/{id} returns 200 for admin")
    void update_admin_returns200() throws Exception {
        when(patientService.update(eq(1L), any())).thenReturn(sampleResponse);
        mockMvc.perform(put("/api/patients/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validRequest)))
            .andExpect(status().isOk());
    }

    @Test @DisplayName("PUT /api/patients/{id} returns 403 for researcher")
    @WithMockUser(roles = "RESEARCHER")
    void update_researcher_returns403() throws Exception {
        mockMvc.perform(put("/api/patients/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validRequest)))
            .andExpect(status().isForbidden());
    }

    @Test @DisplayName("DELETE /api/patients/{id} returns 204 for admin")
    void delete_admin_returns204() throws Exception {
        doNothing().when(patientService).delete(1L);
        mockMvc.perform(delete("/api/patients/1"))
            .andExpect(status().isNoContent());
    }

    @Test @DisplayName("DELETE /api/patients/{id} returns 403 for researcher")
    @WithMockUser(roles = "RESEARCHER")
    void delete_researcher_returns403() throws Exception {
        mockMvc.perform(delete("/api/patients/1"))
            .andExpect(status().isForbidden());
    }
}
