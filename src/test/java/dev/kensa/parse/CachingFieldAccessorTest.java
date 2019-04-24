package dev.kensa.parse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CachingFieldAccessorTest {

    private CachingFieldAccessor accessor;

    @BeforeEach
    void setUp() {
        accessor = new CachingFieldAccessor(new TestInstance(), Set.of("foo", "boo", "noField"));
    }

    @Test
    void canAccessFieldValues() {
        assertThat(accessor.valueOf("foo")).contains("fooValue");
        assertThat(accessor.valueOf("boo")).contains("booValue");
        assertThat(accessor.valueOf("zoo")).isEmpty();
        assertThat(accessor.valueOf("noField")).isEmpty();
    }

    private static class TestInstance {
        private String foo = "fooValue";
        private String boo = "booValue";
    }
}