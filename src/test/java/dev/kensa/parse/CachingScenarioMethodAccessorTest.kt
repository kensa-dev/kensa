package dev.kensa.parse

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CachingScenarioMethodAccessorTest {

    private lateinit var accessor: CachingScenarioMethodAccessor

    @BeforeEach
    internal fun setUp() {
        accessor = CachingScenarioMethodAccessor(JavaTestInstance(), setOf("scenario"))
    }

    @Test
    internal fun `can access scenarios`() {
        assertThat(accessor.valueOf("scenario", "aValue")).isEqualTo("FooBoo!!")
        assertThat(accessor.valueOf("scenario", "aNullValue")).isNull()
        assertThat(accessor.valueOf("nullScenario", "aValue")).isNull()
        assertThat(accessor.valueOf("scenarioDoesNotExist", "foo")).isNull()
    }

    @Test
    internal fun `caches return values`() {
        assertThat(accessor.valueOf("scenario", "aValue")).isEqualTo("FooBoo!!");
        assertThat(accessor.valueOf("scenario", "aValue")).isEqualTo("FooBoo!!");
    }
}