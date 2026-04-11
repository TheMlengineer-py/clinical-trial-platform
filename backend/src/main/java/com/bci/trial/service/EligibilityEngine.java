package com.bci.trial.service;

import com.bci.trial.domain.Patient;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Advanced eligibility rules engine.
 *
 * <p>Evaluates a patient against a study's {@code eligibilityCriteria} string
 * using structured rule parsing instead of ad-hoc string matching.
 *
 * <h2>Criteria Format</h2>
 * <p>Comma-separated rules. Each rule: {@code field operator value}
 * <pre>
 *   "age>18,age<65,condition=NSCLC,condition=Breast cancer"
 * </pre>
 *
 * <h2>Evaluation Logic</h2>
 * <ul>
 *   <li><strong>Age rules</strong>       — AND logic: all age rules must pass</li>
 *   <li><strong>Condition rules</strong> — OR logic: at least one must match</li>
 *   <li><strong>Between groups</strong>  — AND logic: age AND condition must pass</li>
 * </ul>
 */
@Component
public class EligibilityEngine {

    /**
     * Evaluates whether a patient meets all eligibility rules.
     *
     * @param patient  the patient to evaluate
     * @param criteria the study's eligibility criteria string
     * @return a result indicating pass or fail with a reason message
     */
    public EligibilityResult evaluate(Patient patient, String criteria) {
        if (criteria == null || criteria.isBlank()) {
            return EligibilityResult.pass();
        }

        List<EligibilityRule> ageRules       = new ArrayList<>();
        List<EligibilityRule> conditionRules = new ArrayList<>();

        for (String token : criteria.split(",")) {
            EligibilityRule rule = EligibilityRule.parse(token);
            if ("age".equalsIgnoreCase(rule.field())) {
                ageRules.add(rule);
            } else if ("condition".equalsIgnoreCase(rule.field())) {
                conditionRules.add(rule);
            }
        }

        // Age rules — ALL must pass (AND logic)
        for (EligibilityRule rule : ageRules) {
            int threshold = Integer.parseInt(rule.value());
            boolean passes = switch (rule.operator()) {
                case '>' -> patient.getAge() > threshold;
                case '<' -> patient.getAge() < threshold;
                case '=' -> patient.getAge() == threshold;
                default  -> true;
            };
            if (!passes) {
                return EligibilityResult.fail(String.format(
                    "Patient age %d does not satisfy age%c%d",
                    patient.getAge(), rule.operator(), threshold
                ));
            }
        }

        // Condition rules — AT LEAST ONE must pass (OR logic)
        if (!conditionRules.isEmpty()) {
            boolean anyMatch = conditionRules.stream().anyMatch(rule ->
                patient.getCondition().equalsIgnoreCase(rule.value())
            );
            if (!anyMatch) {
                List<String> allowed = conditionRules.stream()
                    .map(EligibilityRule::value)
                    .toList();
                return EligibilityResult.fail(String.format(
                    "Patient condition '%s' does not match any required condition: %s",
                    patient.getCondition(), allowed
                ));
            }
        }

        return EligibilityResult.pass();
    }

    // ── Result type ───────────────────────────────────────────────────────────

    /**
     * Immutable result returned by {@link #evaluate}.
     * Carries a human-readable reason on failure for use in error messages.
     */
    public record EligibilityResult(boolean eligible, String reason) {

        public static EligibilityResult pass() {
            return new EligibilityResult(true, null);
        }

        public static EligibilityResult fail(String reason) {
            return new EligibilityResult(false, reason);
        }
    }
}
