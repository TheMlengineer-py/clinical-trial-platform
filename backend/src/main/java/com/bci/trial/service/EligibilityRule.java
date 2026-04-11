package com.bci.trial.service;

/**
 * Represents a single parsed eligibility rule.
 *
 * <p>Rules are parsed from the study's {@code eligibilityCriteria} string.
 * Format: {@code field operator value}
 *
 * <p>Examples:
 * <ul>
 *   <li>{@code age>18}          → field=age, operator=>, value=18</li>
 *   <li>{@code age<65}          → field=age, operator=<, value=65</li>
 *   <li>{@code condition=NSCLC} → field=condition, operator==, value=NSCLC</li>
 * </ul>
 *
 * @param field    the patient field to evaluate (age, condition)
 * @param operator the comparison operator (>, <, =)
 * @param value    the threshold or required value as a string
 */
public record EligibilityRule(String field, char operator, String value) {

    /**
     * Parses a single rule token into an {@link EligibilityRule}.
     *
     * @param token a single rule string e.g. "age>18" or "condition=NSCLC"
     * @return the parsed rule
     * @throws IllegalArgumentException if the token cannot be parsed
     */
    public static EligibilityRule parse(String token) {
        token = token.trim();

        for (char op : new char[]{'>', '<', '='}) {
            int idx = token.indexOf(op);
            if (idx > 0 && idx < token.length() - 1) {
                String field = token.substring(0, idx).trim();
                String value = token.substring(idx + 1).trim();
                return new EligibilityRule(field, op, value);
            }
        }

        throw new IllegalArgumentException(
            "Cannot parse eligibility rule token: '" + token + "'"
        );
    }
}
