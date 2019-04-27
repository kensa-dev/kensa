package dev.kensa.parse;

import dev.kensa.KensaException;
import dev.kensa.render.Renderers;
import dev.kensa.util.NamedValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValueAccessorsTest {

    private ValueAccessors accessors;

    @BeforeEach
    void setUp() {
        TestInstance testInstance = new TestInstance();

        accessors = new ValueAccessors(
                new Renderers(),
                new CachingScenarioMethodAccessor(testInstance, Set.of("scenario1", "scenario2")),
                new CachingFieldAccessor(testInstance, Set.of("field1", "field2")),
                new ParameterAccessor(Set.of(new NamedValue("n1", "v1"), new NamedValue("n2", "v2")))
        );
    }

    @Test
    void canAccessValues() {
        // Fields
        assertThat(accessors.realValueOf("field1")).contains("foo");
        assertThat(accessors.realValueOf("field2")).contains("111");

        // Scenarios
        assertThat(accessors.realValueOf("scenario1", "scenario1Value")).contains("scenario1Value");
        assertThat(accessors.realValueOf("scenario2", "scenario2Value")).contains("scenario2Value");

        // Resolves parameters before fields if present
        assertThat(accessors.realValueOf("n1")).contains("v1");

        // Nulls etc
        assertThat(accessors.realValueOf("whateva")).isEmpty();
        assertThat(accessors.realValueOf("nullField")).isEmpty();
        assertThat(accessors.realValueOf("nullScenario", "scenario2Value")).isEmpty();

        assertThatThrownBy(() -> accessors.realValueOf("scenario1", "whateva")).isInstanceOf(KensaException.class);
    }

    private static class TestInstance {
        private final Scenario1 scenario1 = new Scenario1();
        private final Scenario2 scenario2 = new Scenario2();
        private final Scenario2 nullScenario = null;

        private final String field1 = "foo";
        private final Integer field2 = 111;
        private final String n1 = "n1FieldValue";
        private final String nullField = null;
    }

    private static class Scenario1 {

        public String scenario1Value() {
            return "scenario1Value";
        }
    }

    private static class Scenario2 {

        public String scenario2Value() {
            return "scenario2Value";
        }
    }
}