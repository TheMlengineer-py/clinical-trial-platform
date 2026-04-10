package com.bci.trial.event;

import java.time.Instant;

/**
 * Domain event fired after a patient is successfully recruited into a study.
 *
 * <p>Declared as a record for immutability — events should never be mutated
 * after they are created. This event is the extension point for future
 * integrations: audit logging, email notifications, WebSocket broadcasts, etc.
 *
 * <p>Currently handled by {@link DomainEventPublisher} which logs it to the
 * application log. Additional handlers can be registered with
 * {@code @EventListener} without modifying the recruitment flow.
 *
 * @param patientId  ID of the patient who was recruited
 * @param studyId    ID of the study they were recruited into
 * @param occurredAt wall-clock timestamp of the recruitment action
 */
public record PatientRecruitedEvent(
    Long patientId,
    Long studyId,
    Instant occurredAt
) {}
