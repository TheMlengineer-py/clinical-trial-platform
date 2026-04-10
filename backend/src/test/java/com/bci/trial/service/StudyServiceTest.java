package com.bci.trial.service;

import com.bci.trial.domain.*;
import com.bci.trial.dto.*;
import com.bci.trial.exception.*;
import com.bci.trial.repository.StudyRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StudyService}.
 *
 * <p>Uses Mockito to isolate the service from the real database.
 * No Spring context is loaded — tests run fast and independently.
 */
@ExtendWith(MockitoExtension.class)
class StudyServiceTest {

    @Mock  StudyRepository studyRepository;
    @InjectMocks StudyService studyService;

    // ── create() ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create() saves a study in DRAFT status")
    void create_savesDraftStudy() {
        StudyRequest req = new StudyRequest();
        req.setTitle("Test Study");
        req.setMaxEnrollment(10);

        Study saved = Study.builder().id(1L).title("Test Study")
            .maxEnrollment(10).status(StudyStatus.DRAFT).build();
        when(studyRepository.save(any())).thenReturn(saved);

        StudyResponse response = studyService.create(req);

        assertThat(response.status()).isEqualTo(StudyStatus.DRAFT);
        assertThat(response.title()).isEqualTo("Test Study");
    }

    // ── transitionStatus() ───────────────────────────────────────────────────

    @Test
    @DisplayName("transitionStatus() advances DRAFT to OPEN successfully")
    void transition_draftToOpen_succeeds() {
        Study study = Study.builder().id(1L).status(StudyStatus.DRAFT)
            .title("S").maxEnrollment(5).build();
        when(studyRepository.findById(1L)).thenReturn(Optional.of(study));
        when(studyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        StudyResponse result = studyService.transitionStatus(1L, StudyStatus.OPEN);

        assertThat(result.status()).isEqualTo(StudyStatus.OPEN);
    }

    @Test
    @DisplayName("transitionStatus() rejects illegal transitions (DRAFT → CLOSED)")
    void transition_illegalJump_throws() {
        Study study = Study.builder().id(1L).status(StudyStatus.DRAFT)
            .title("S").maxEnrollment(5).build();
        when(studyRepository.findById(1L)).thenReturn(Optional.of(study));

        assertThatThrownBy(() -> studyService.transitionStatus(1L, StudyStatus.CLOSED))
            .isInstanceOf(InvalidStateTransitionException.class)
            .hasMessageContaining("DRAFT")
            .hasMessageContaining("CLOSED");
    }

    @Test
    @DisplayName("transitionStatus() rejects reversal (OPEN → DRAFT)")
    void transition_reversal_throws() {
        Study study = Study.builder().id(1L).status(StudyStatus.OPEN)
            .title("S").maxEnrollment(5).build();
        when(studyRepository.findById(1L)).thenReturn(Optional.of(study));

        assertThatThrownBy(() -> studyService.transitionStatus(1L, StudyStatus.DRAFT))
            .isInstanceOf(InvalidStateTransitionException.class);
    }

    // ── delete() ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete() throws when study is OPEN")
    void delete_openStudy_throws() {
        Study study = Study.builder().id(1L).status(StudyStatus.OPEN)
            .title("S").maxEnrollment(5).build();
        when(studyRepository.findById(1L)).thenReturn(Optional.of(study));

        assertThatThrownBy(() -> studyService.delete(1L))
            .isInstanceOf(StudyDeletionNotAllowedException.class);
    }

    @Test
    @DisplayName("delete() succeeds for a DRAFT study")
    void delete_draftStudy_succeeds() {
        Study study = Study.builder().id(1L).status(StudyStatus.DRAFT)
            .title("S").maxEnrollment(5).build();
        when(studyRepository.findById(1L)).thenReturn(Optional.of(study));

        assertThatCode(() -> studyService.delete(1L)).doesNotThrowAnyException();
        verify(studyRepository).delete(study);
    }

    // ── findById() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById() throws StudyNotFoundException for unknown ID")
    void findById_unknownId_throws() {
        when(studyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyService.findById(99L))
            .isInstanceOf(StudyNotFoundException.class);
    }
}
