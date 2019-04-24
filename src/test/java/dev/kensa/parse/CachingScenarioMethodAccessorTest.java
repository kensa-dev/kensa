package dev.kensa.parse;

import dev.kensa.KensaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CachingScenarioMethodAccessorTest {

    private CachingScenarioMethodAccessor accessor;

    @BeforeEach
    void setUp() {
        accessor = new CachingScenarioMethodAccessor(new TestInstance(), Set.of("scenario"));
    }

    @Test
    void canAccessScenarios() {
        assertThat(accessor.valueOf("scenario", "aValue")).contains("FooBoo!!");
        assertThat(accessor.valueOf("scenario", "aNullValue")).isEmpty();
        assertThat(accessor.valueOf("nullScenario", "aValue")).isEmpty();
        assertThat(accessor.valueOf("scenarioDoesNotExist", "foo")).isEmpty();
        assertThatThrownBy(() ->accessor.valueOf("scenario", "methodDoesNotExist")).isInstanceOf(KensaException.class);
    }

    @Test
    void canCacheReturnValues() {
        assertThat(accessor.valueOf("scenario", "aValue")).contains("FooBoo!!");
        assertThat(accessor.valueOf("scenario", "aValue")).contains("FooBoo!!");
    }

    private static class TestInstance {
        private TestScenario scenario = new TestScenario();
        private TestScenario nullScenario = null;
    }

    private static class TestScenario {
        private String value = "FooBoo!!";

        public String aValue() {
            String value = this.value;
            this.value = "Meh!";
            return value;
        }
        public String aNullValue() {
            return null;
        }
    }
}