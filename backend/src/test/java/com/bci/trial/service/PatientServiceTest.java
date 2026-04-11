package com.bci.trial.service;

import com.bci.trial.domain.Patient;
import com.bci.trial.dto.PatientRequest;
import com.bci.trial.dto.PatientResponse;
import com.bci.trial.exception.PatientNotFoundException;
import com.bci.trial.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PatientService}.
 *
 * <p>Mockito isolates the service from the real database.
 * No Spring context — tests run in milliseconds.
 *
 * <p>Nested classes group related tests so the output is easy to scan:
 * each @Nested class maps to one service method.
 */
@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    PatientRepository patientRepository;

    @InjectMocks
    PatientService patientService;

    // ── Shared fixtures ───────────────────────────────────────────────────────

    /** A saved patient entity used as the baseline for most tests. */
    private Patient savedPatient;

    @BeforeEach
    void setUp() {
        savedPatient = Patient.builder()
            .id(1L)
            .name("Alice Nwosu")
            .age(42)
            .condition("Breast cancer")
            .build();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // findById()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("returns the patient DTO when the ID exists")
        void found_returnsResponse() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(savedPatient));

            PatientResponse result = patientService.findById(1L);

            assertThat(result.name()).isEqualTo("Alice Nwosu");
            assertThat(result.age()).isEqualTo(42);
            assertThat(result.condition()).isEqualTo("Breast cancer");
        }

        @Test
        @DisplayName("throws PatientNotFoundException when ID does not exist")
        void notFound_throws() {
            when(patientRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> patientService.findById(99L))
                .isInstanceOf(PatientNotFoundException.class)
                .hasMessageContaining("99");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // create()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("saves a new patient with no enrolment and returns DTO")
        void create_savesAndReturns() {
            PatientRequest req = new PatientRequest();
            req.setName("Bob Mensah");
            req.setAge(55);
            req.setCondition("NSCLC");

            Patient persisted = Patient.builder()
                .id(2L).name("Bob Mensah").age(55).condition("NSCLC").build();
            when(patientRepository.save(any())).thenReturn(persisted);

            PatientResponse result = patientService.create(req);

            assertThat(result.id()).isEqualTo(2L);
            assertThat(result.enrolledStudyId()).isNull();
            verify(patientRepository).save(any(Patient.class));
        }

        @Test
        @DisplayName("new patient has null recruitedAt (never recruited)")
        void create_recruitedAtIsNull() {
            PatientRequest req = new PatientRequest();
            req.setName("Carol"); req.setAge(30); req.setCondition("Melanoma");

            Patient persisted = Patient.builder()
                .id(3L).name("Carol").age(30).condition("Melanoma").build();
            when(patientRepository.save(any())).thenReturn(persisted);

            PatientResponse result = patientService.create(req);
            assertThat(result.recruitedAt()).isNull();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // update()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("updates name, age, and condition from the request")
        void update_fieldsChangedCorrectly() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(savedPatient));
            when(patientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            PatientRequest req = new PatientRequest();
            req.setName("Alice Updated");
            req.setAge(43);
            req.setCondition("Colorectal");

            PatientResponse result = patientService.update(1L, req);

            assertThat(result.name()).isEqualTo("Alice Updated");
            assertThat(result.age()).isEqualTo(43);
            assertThat(result.condition()).isEqualTo("Colorectal");
        }

        @Test
        @DisplayName("update() does not change enrolledStudyId")
        void update_doesNotClearEnrolment() {
            savedPatient.setEnrolledStudyId(5L);
            when(patientRepository.findById(1L)).thenReturn(Optional.of(savedPatient));
            when(patientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            PatientRequest req = new PatientRequest();
            req.setName("Alice"); req.setAge(42); req.setCondition("Breast cancer");

            PatientResponse result = patientService.update(1L, req);

            assertThat(result.enrolledStudyId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("throws PatientNotFoundException for unknown ID")
        void update_unknownId_throws() {
            when(patientRepository.findById(99L)).thenReturn(Optional.empty());

            PatientRequest req = new PatientRequest();
            req.setName("X"); req.setAge(20); req.setCondition("Y");

            assertThatThrownBy(() -> patientService.update(99L, req))
                .isInstanceOf(PatientNotFoundException.class);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // delete()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("soft-deletes a patient by stamping deletedAt")
        void delete_existingPatient_succeeds() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(savedPatient));
            when(patientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            assertThatCode(() -> patientService.delete(1L)).doesNotThrowAnyException();

            // Verify soft delete — save() called with deletedAt stamped
            verify(patientRepository).save(savedPatient);
            assertThat(savedPatient.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("throws PatientNotFoundException for unknown ID")
        void delete_unknownId_throws() {
            when(patientRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> patientService.delete(99L))
                .isInstanceOf(PatientNotFoundException.class);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // findAll()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("returns all patients when no condition filter is given")
        void findAll_noFilter_returnsAll() {
            var pageable = PageRequest.of(0, 10);
            when(patientRepository.findAllSortedByRecruitedAt(pageable))
                .thenReturn(new PageImpl<>(List.of(savedPatient)));

            var result = patientService.findAll(null, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("Alice Nwosu");
        }

        @Test
        @DisplayName("delegates to condition filter query when condition is provided")
        void findAll_withCondition_delegatesToFilterQuery() {
            var pageable = PageRequest.of(0, 10);
            when(patientRepository.findByConditionContainingIgnoreCase("breast", pageable))
                .thenReturn(new PageImpl<>(List.of(savedPatient)));

            var result = patientService.findAll("breast", pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(patientRepository).findByConditionContainingIgnoreCase("breast", pageable);
            verify(patientRepository, never()).findAllSortedByRecruitedAt(any());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // meetsEligibility()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("meetsEligibility()")
    class MeetsEligibility {

        private final Patient p = Patient.builder()
            .age(42).condition("Breast cancer").build();

        @Test
        @DisplayName("passes when criteria string is null")
        void nullCriteria_passes() {
            assertThat(patientService.meetsEligibility(p, null)).isTrue();
        }

        @Test
        @DisplayName("passes when criteria string is blank")
        void blankCriteria_passes() {
            assertThat(patientService.meetsEligibility(p, "   ")).isTrue();
        }

        @Test
        @DisplayName("passes age > 18 for a 42-year-old")
        void ageGreaterThan_passes() {
            assertThat(patientService.meetsEligibility(p, "age>18")).isTrue();
        }

        @Test
        @DisplayName("fails age > 50 for a 42-year-old")
        void ageGreaterThan_fails() {
            assertThat(patientService.meetsEligibility(p, "age>50")).isFalse();
        }

        @Test
        @DisplayName("passes condition=Breast cancer (exact, case-insensitive)")
        void conditionMatch_passes() {
            assertThat(patientService.meetsEligibility(p, "condition=Breast cancer")).isTrue();
        }

        @Test
        @DisplayName("fails condition=NSCLC for a Breast cancer patient")
        void conditionMatch_fails() {
            assertThat(patientService.meetsEligibility(p, "condition=NSCLC")).isFalse();
        }

        @Test
        @DisplayName("passes combined age and condition criteria when both match")
        void combined_bothMatch_passes() {
            assertThat(patientService.meetsEligibility(p, "age>18,condition=Breast cancer")).isTrue();
        }

        @Test
        @DisplayName("fails combined criteria when one rule fails")
        void combined_oneRuleFails_fails() {
            assertThat(patientService.meetsEligibility(p, "age>18,condition=NSCLC")).isFalse();
        }
    }
}
