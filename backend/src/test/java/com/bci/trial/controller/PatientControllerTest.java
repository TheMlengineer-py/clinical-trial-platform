package com.bci.trial.controller;

import com.bci.trial.dto.PatientRequest;
import com.bci.trial.dto.PatientResponse;
import com.bci.trial.exception.PatientNotFoundException;
import com.bci.trial.service.PatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link PatientController} using MockMvc.
 *
 * <p>{@code @WebMvcTest} loads only the web layer — no full Spring context,
 * no real database. PatientService is mocked so these tests verify:
 * <ul>
 *   <li>Correct HTTP status codes for all outcomes</li>
 *   <li>Request body validation (400 on missing fields)</li>
 *   <li>JSON serialisation of response payloads</li>
 *   <li>404 handling for unknown patient IDs</li>
 * </ul>
 */
@WebMvcTest(PatientController.class)
class PatientControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    PatientService patientService;

    /** Reusable sample response returned by mocked service methods. */
    private PatientResponse sampleResponse;

    /** Reusable valid request body. */
    private PatientRequest validRequest;

    @BeforeEach
    void setUp() {
        sampleResponse = new PatientResponse(
            1L,
            "Alice Nwosu",
            42,
            "Breast cancer",
            null,           // not enrolled
            Instant.now()
        );

        validRequest = new PatientRequest();
        validRequest.setName("Alice Nwosu");
        validRequest.setAge(42);
        validRequest.setCondition("Breast cancer");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // GET /api/patients
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/patients")
    class ListPatients {

        @Test
        @DisplayName("returns 200 with paginated patient list")
        void list_returns200() throws Exception {
            when(patientService.findAll(any(), any()))
                .thenReturn(new PageImpl<>(List.of(sampleResponse)));

            mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Alice Nwosu"))
                .andExpect(jsonPath("$.content[0].condition").value("Breast cancer"));
        }

        @Test
        @DisplayName("passes condition query param to service")
        void list_withConditionFilter_delegates() throws Exception {
            when(patientService.findAll(eq("NSCLC"), any()))
                .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(get("/api/patients").param("condition", "NSCLC"))
                .andExpect(status().isOk());

            verify(patientService).findAll(eq("NSCLC"), any());
        }

        @Test
        @DisplayName("returns empty content when no patients match filter")
        void list_noResults_returnsEmptyPage() throws Exception {
            when(patientService.findAll(any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(get("/api/patients").param("condition", "Unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // GET /api/patients/{id}
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/patients/{id}")
    class GetById {

        @Test
        @DisplayName("returns 200 with patient detail for a known ID")
        void getById_known_returns200() throws Exception {
            when(patientService.findById(1L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/patients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice Nwosu"))
                .andExpect(jsonPath("$.age").value(42));
        }

        @Test
        @DisplayName("returns 404 when patient ID does not exist")
        void getById_unknown_returns404() throws Exception {
            when(patientService.findById(99L))
                .thenThrow(new PatientNotFoundException(99L));

            mockMvc.perform(get("/api/patients/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Patient not found with id: 99"));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // POST /api/patients
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/patients")
    class CreatePatient {

        @Test
        @DisplayName("returns 201 Created with patient body on valid request")
        void create_valid_returns201() throws Exception {
            when(patientService.create(any())).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/patients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Alice Nwosu"));
        }

        @Test
        @DisplayName("returns 400 when name is blank")
        void create_blankName_returns400() throws Exception {
            validRequest.setName(""); // violates @NotBlank

            mockMvc.perform(post("/api/patients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("returns 400 when condition is blank")
        void create_blankCondition_returns400() throws Exception {
            validRequest.setCondition(""); // violates @NotBlank

            mockMvc.perform(post("/api/patients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when age is negative")
        void create_negativeAge_returns400() throws Exception {
            validRequest.setAge(-1); // violates @Min(0)

            mockMvc.perform(post("/api/patients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when age exceeds 130")
        void create_ageOver130_returns400() throws Exception {
            validRequest.setAge(200); // violates @Max(130)

            mockMvc.perform(post("/api/patients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PUT /api/patients/{id}
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /api/patients/{id}")
    class UpdatePatient {

        @Test
        @DisplayName("returns 200 with updated patient on valid request")
        void update_valid_returns200() throws Exception {
            when(patientService.update(eq(1L), any())).thenReturn(sampleResponse);

            mockMvc.perform(put("/api/patients/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Nwosu"));
        }

        @Test
        @DisplayName("returns 404 when updating a non-existent patient")
        void update_unknown_returns404() throws Exception {
            when(patientService.update(eq(99L), any()))
                .thenThrow(new PatientNotFoundException(99L));

            mockMvc.perform(put("/api/patients/99")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 400 on invalid update body")
        void update_invalidBody_returns400() throws Exception {
            validRequest.setName(""); // blank name

            mockMvc.perform(put("/api/patients/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // DELETE /api/patients/{id}
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DELETE /api/patients/{id}")
    class DeletePatient {

        @Test
        @DisplayName("returns 204 No Content on successful delete")
        void delete_existing_returns204() throws Exception {
            doNothing().when(patientService).delete(1L);

            mockMvc.perform(delete("/api/patients/1"))
                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("returns 404 when deleting a non-existent patient")
        void delete_unknown_returns404() throws Exception {
            doThrow(new PatientNotFoundException(99L)).when(patientService).delete(99L);

            mockMvc.perform(delete("/api/patients/99"))
                .andExpect(status().isNotFound());
        }
    }
}
