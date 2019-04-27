package dev.kensa.parse;

import dev.kensa.util.NamedValue;
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
                new NamedValue("n1", "v1"),
                new NamedValue("n2", "v2"),
                new NamedValue("n3", "v3"),
                new NamedValue("n4", "v4"),
                new NamedValue("n5", null)
        ));
    }

    @Test
    void canAccessParameters() {
        assertThat(accessor.valueOf("n1")).contains("v1");
        assertThat(accessor.valueOf("n5")).contains(NullValue);
        assertThat(accessor.valueOf("foo")).contains(NotCached);
    }
}