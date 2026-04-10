package com.bci.trial.service;

import com.bci.trial.domain.*;
import com.bci.trial.dto.*;
import com.bci.trial.event.*;
import com.bci.trial.exception.*;
import com.bci.trial.repository.StudyRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RecruitmentService}.
 *
 * <p>Covers all edge cases required by the assessment:
 * <ul>
 *   <li>Successful recruitment</li>
 *   <li>Study not open (non-OPEN status)</li>
 *   <li>Patient already enrolled</li>
 *   <li>Capacity exceeded (over-enrollment)</li>
 *   <li>Eligibility failure</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class RecruitmentServiceTest {

    @Mock StudyRepository      studyRepository;
    @Mock PatientService       patientService;
    @Mock DomainEventPublisher eventPublisher;
    @InjectMocks RecruitmentService recruitmentService;

    private Patient patient;
    private Study   study;
    private RecruitmentRequest req;

    @BeforeEach
    void setUp() {
        patient = Patient.builder().id(1L).name("Alice").age(30)
            .condition("NSCLC").build(); // not enrolled

        study = Study.builder().id(2L).title("Lung Trial")
            .status(StudyStatus.OPEN)
            .maxEnrollment(5).currentEnrollment(3)
            .eligibilityCriteria("age>18,condition=NSCLC")
            .build();

        req = new RecruitmentRequest();
        req.setPatientId(1L);
        req.setStudyId(2L);

        when(patientService.getOrThrow(1L)).thenReturn(patient);
        when(studyRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(study));
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("recruit() enrolls patient and increments study count")
    void recruit_success() {
        PatientResponse mockResponse = new PatientResponse(1L, "Alice", 30, "NSCLC", 2L, null);
        when(patientService.meetsEligibility(patient, study.getEligibilityCriteria())).thenReturn(true);
        when(patientService.toResponse(any())).thenReturn(mockResponse);

        PatientResponse result = recruitmentService.recruit(req);

        assertThat(result.enrolledStudyId()).isEqualTo(2L);
        assertThat(study.getCurrentEnrollment()).isEqualTo(4); // was 3, now 4
        verify(eventPublisher).publish(any(PatientRecruitedEvent.class));
    }

    // ── Guard: study not OPEN ─────────────────────────────────────────────────

    @Test
    @DisplayName("recruit() throws StudyNotOpenException when study is DRAFT")
    void recruit_studyNotOpen_throws() {
        study.setStatus(StudyStatus.DRAFT);

        assertThatThrownBy(() -> recruitmentService.recruit(req))
            .isInstanceOf(StudyNotOpenException.class);

        verify(eventPublisher, never()).publish(any());
    }

    // ── Guard: patient already enrolled ──────────────────────────────────────

    @Test
    @DisplayName("recruit() throws PatientAlreadyEnrolledException when patient has study")
    void recruit_alreadyEnrolled_throws() {
        patient.setEnrolledStudyId(99L); // already in a different study

        assertThatThrownBy(() -> recruitmentService.recruit(req))
            .isInstanceOf(PatientAlreadyEnrolledException.class);
    }

    // ── Guard: capacity exceeded ──────────────────────────────────────────────

    @Test
    @DisplayName("recruit() throws EnrollmentCapacityException when study is full")
    void recruit_capacityExceeded_throws() {
        study.setCurrentEnrollment(5); // equals maxEnrollment=5, so full

        assertThatThrownBy(() -> recruitmentService.recruit(req))
            .isInstanceOf(EnrollmentCapacityException.class);
    }

    // ── Guard: eligibility failure ────────────────────────────────────────────

    @Test
    @DisplayName("recruit() throws EligibilityException when patient is ineligible")
    void recruit_ineligiblePatient_throws() {
        when(patientService.meetsEligibility(patient, study.getEligibilityCriteria()))
            .thenReturn(false);

        assertThatThrownBy(() -> recruitmentService.recruit(req))
            .isInstanceOf(EligibilityException.class);
    }
}
