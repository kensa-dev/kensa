package dev.kensa.parse;

import dev.kensa.util.NameValuePair;
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
                new NameValuePair("n1", "v1"),
                new NameValuePair("n2", "v2"),
                new NameValuePair("n3", "v3"),
                new NameValuePair("n4", "v4"),
                new NameValuePair("n5", null)
        ));
    }

    @Test
    void canAccessParameters() {
        assertThat(accessor.valueOf("n1")).contains("v1");
        assertThat(accessor.valueOf("n5")).contains(NullValue);
        assertThat(accessor.valueOf("foo")).contains(NotCached);
    }
}