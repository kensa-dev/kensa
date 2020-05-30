package dev.kensa.parse

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CachingFieldAccessorTest {
    private lateinit var accessor: CachingFieldAccessor

    @BeforeEach
    internal fun setUp() {
        accessor = CachingFieldAccessor(TestInstance(), setOf("foo", "boo", "noField"))
    }

    @Test
    internal fun canAccessFieldValues() {
        assertThat(accessor.valueOf("foo")).isEqualTo("fooValue")
        assertThat(accessor.valueOf("boo")).isEqualTo("booValue")
        assertThat(accessor.valueOf("zoo")).isNull()
    }

    private class TestInstance {
        private val foo = "fooValue"
        private val boo = "booValue"
    }
}