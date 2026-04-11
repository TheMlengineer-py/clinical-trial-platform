package com.bci.trial.service;

import com.bci.trial.domain.Patient;
import com.bci.trial.service.EligibilityEngine.EligibilityResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link EligibilityEngine}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>Null and blank criteria → always pass</li>
 *   <li>Single age rules (>, <, =)</li>
 *   <li>Age range rules (AND logic)</li>
 *   <li>Single condition rule</li>
 *   <li>Multiple condition rules (OR logic)</li>
 *   <li>Combined age + condition rules</li>
 *   <li>Failure reason messages</li>
 * </ul>
 */
class EligibilityEngineTest {

    private final EligibilityEngine engine = new EligibilityEngine();

    private Patient patient(int age, String condition) {
        return Patient.builder().age(age).condition(condition).build();
    }

    @Nested
    @DisplayName("null and blank criteria")
    class NullBlank {

        @Test
        @DisplayName("null criteria passes all patients")
        void nullCriteria_passes() {
            assertThat(engine.evaluate(patient(42, "NSCLC"), null).eligible())
                .isTrue();
        }

        @Test
        @DisplayName("blank criteria passes all patients")
        void blankCriteria_passes() {
            assertThat(engine.evaluate(patient(42, "NSCLC"), "   ").eligible())
                .isTrue();
        }
    }

    @Nested
    @DisplayName("age rules")
    class AgeRules {

        @Test
        @DisplayName("age>18 passes for patient aged 42")
        void ageGreaterThan_passes() {
            assertThat(engine.evaluate(patient(42, "NSCLC"), "age>18").eligible())
                .isTrue();
        }

        @Test
        @DisplayName("age>18 fails for patient aged 17 with reason message")
        void ageGreaterThan_fails() {
            EligibilityResult result = engine.evaluate(patient(17, "NSCLC"), "age>18");
            assertThat(result.eligible()).isFalse();
            assertThat(result.reason()).contains("17").contains("age>18");
        }

        @Test
        @DisplayName("age<65 passes for patient aged 42")
        void ageLessThan_passes() {
            assertThat(engine.evaluate(patient(42, "NSCLC"), "age<65").eligible())
                .isTrue();
        }

        @Test
        @DisplayName("age<65 fails for patient aged 70 with reason message")
        void ageLessThan_fails() {
            EligibilityResult result = engine.evaluate(patient(70, "NSCLC"), "age<65");
            assertThat(result.eligible()).isFalse();
            assertThat(result.reason()).contains("70").contains("age<65");
        }

        @Test
        @DisplayName("age=42 passes for patient aged exactly 42")
        void ageEquals_passes() {
            assertThat(engine.evaluate(patient(42, "NSCLC"), "age=42").eligible())
                .isTrue();
        }

        @Test
        @DisplayName("age range age>18,age<65 passes for patient aged 42")
        void ageRange_passes() {
            assertThat(engine.evaluate(patient(42, "NSCLC"), "age>18,age<65").eligible())
                .isTrue();
        }

        @Test
        @DisplayName("age range age>18,age<65 fails for patient aged 70")
        void ageRange_upperBound_fails() {
            EligibilityResult result = engine.evaluate(patient(70, "NSCLC"), "age>18,age<65");
            assertThat(result.eligible()).isFalse();
            assertThat(result.reason()).contains("70");
        }
    }

    @Nested
    @DisplayName("condition rules")
    class ConditionRules {

        @Test
        @DisplayName("single condition match passes")
        void singleCondition_passes() {
            assertThat(engine.evaluate(
                patient(42, "NSCLC"), "condition=NSCLC"
            ).eligible()).isTrue();
        }

        @Test
        @DisplayName("single condition mismatch fails with reason")
        void singleCondition_fails() {
            EligibilityResult result = engine.evaluate(
                patient(42, "Breast cancer"), "condition=NSCLC"
            );
            assertThat(result.eligible()).isFalse();
            assertThat(result.reason())
                .contains("Breast cancer")
                .contains("NSCLC");
        }

        @Test
        @DisplayName("condition match is case-insensitive")
        void conditionMatch_caseInsensitive() {
            assertThat(engine.evaluate(
                patient(42, "nsclc"), "condition=NSCLC"
            ).eligible()).isTrue();
        }

        @Test
        @DisplayName("multiple conditions use OR logic — first match passes")
        void multipleConditions_orLogic_firstMatch() {
            assertThat(engine.evaluate(
                patient(42, "NSCLC"),
                "condition=NSCLC,condition=Breast cancer"
            ).eligible()).isTrue();
        }

        @Test
        @DisplayName("multiple conditions use OR logic — second match passes")
        void multipleConditions_orLogic_secondMatch() {
            assertThat(engine.evaluate(
                patient(42, "Breast cancer"),
                "condition=NSCLC,condition=Breast cancer"
            ).eligible()).isTrue();
        }

        @Test
        @DisplayName("multiple conditions fail when none match")
        void multipleConditions_noneMatch_fails() {
            EligibilityResult result = engine.evaluate(
                patient(42, "Melanoma"),
                "condition=NSCLC,condition=Breast cancer"
            );
            assertThat(result.eligible()).isFalse();
            assertThat(result.reason())
                .contains("Melanoma")
                .contains("NSCLC")
                .contains("Breast cancer");
        }
    }

    @Nested
    @DisplayName("combined age and condition rules")
    class Combined {

        @Test
        @DisplayName("passes when both age and condition rules are satisfied")
        void combined_bothPass() {
            assertThat(engine.evaluate(
                patient(42, "Breast cancer"),
                "age>18,condition=Breast cancer"
            ).eligible()).isTrue();
        }

        @Test
        @DisplayName("fails when age passes but condition fails")
        void combined_agePasses_conditionFails() {
            EligibilityResult result = engine.evaluate(
                patient(42, "Melanoma"),
                "age>18,condition=NSCLC"
            );
            assertThat(result.eligible()).isFalse();
            assertThat(result.reason()).contains("Melanoma");
        }

        @Test
        @DisplayName("fails when condition passes but age fails")
        void combined_conditionPasses_ageFails() {
            EligibilityResult result = engine.evaluate(
                patient(17, "NSCLC"),
                "age>18,condition=NSCLC"
            );
            assertThat(result.eligible()).isFalse();
            assertThat(result.reason()).contains("17");
        }

        @Test
        @DisplayName("full complex criteria — age range + multiple conditions")
        void complex_ageRangeAndMultipleConditions() {
            assertThat(engine.evaluate(
                patient(42, "Breast cancer"),
                "age>18,age<65,condition=NSCLC,condition=Breast cancer"
            ).eligible()).isTrue();
        }
    }
}
