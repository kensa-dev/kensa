package dev.kensa.parse;

import dev.kensa.util.DisplayableNamedValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static dev.kensa.parse.CacheState.NotCached;
import static dev.kensa.parse.CacheState.NullValue;
import static org.assertj.core.api.Assertions.assertThat;

class ParameterAccessorTest {

    private ParameterAccessor accessor;

    @BeforeEach
    void setUp() {
        accessor = new ParameterAccessor(Set.of(
                new DisplayableNamedValue("n1", "n 1", "v1"),
                new DisplayableNamedValue("n2", "n 2", "v2"),
                new DisplayableNamedValue("n3", "n 3", "v3"),
                new DisplayableNamedValue("n4", "n 4", "v4"),
                new DisplayableNamedValue("n5", "n 5", null)
        ));
    }

    @Test
    void canAccessParameters() {
        assertThat(accessor.valueOf("n1")).contains("v1");
        assertThat(accessor.valueOf("n5")).contains(NullValue);
        assertThat(accessor.valueOf("foo")).contains(NotCached);
    }
}