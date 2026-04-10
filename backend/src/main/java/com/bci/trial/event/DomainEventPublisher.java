package com.bci.trial.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper around Spring's {@link ApplicationEventPublisher}.
 *
 * <p>Separates the act of publishing events (called from {@code RecruitmentService})
 * from the act of handling them (this class, and any future {@code @EventListener}s).
 * This keeps the service layer unaware of what listeners exist.
 *
 * <p>To add a new side-effect on recruitment (e.g. send an email, push a WebSocket
 * message), add a new {@code @EventListener} method here or in a new {@code @Component}
 * — no changes needed in the service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Publishes a {@link PatientRecruitedEvent} to all registered Spring listeners.
     * Called by {@code RecruitmentService} immediately after a successful recruitment.
     *
     * @param event the event to publish
     */
    public void publish(PatientRecruitedEvent event) {
        eventPublisher.publishEvent(event);
    }

    /**
     * Handles the {@link PatientRecruitedEvent} — currently logs it.
     *
     * <p>In production this would fan out to: an audit log table, an email
     * notification service, a WebSocket topic, or an external message broker.
     * For the assessment, structured logging is sufficient and demonstrates
     * the pattern correctly.
     *
     * @param event the recruitment event received from the publisher
     */
    @EventListener
    public void onPatientRecruited(PatientRecruitedEvent event) {
        log.info("[DOMAIN EVENT] PatientRecruited — patientId={} studyId={} at={}",
            event.patientId(), event.studyId(), event.occurredAt());
    }
}
