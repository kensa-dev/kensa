package dev.kensa.parse

import dev.kensa.parse.Accessor.ValueAccessor.PropertyAccessor
import dev.kensa.util.allProperties
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CachingScenarioMethodAccessorTest {

    private lateinit var accessor: CachingScenarioMethodAccessor

    @BeforeEach
    internal fun setUp() {
        accessor = CachingScenarioMethodAccessor(JavaTestInstance(),
            JavaTestInstance::class.allProperties.map { PropertyAccessor(it) }.associateBy { it.name })
    }

    @Test
    internal fun `can access scenarios`() {
        accessor.valueOf("scenario", "aValue") shouldBe "FooBoo!!"
        accessor.valueOf("scenario", "aNullValue").shouldBeNull()
        accessor.valueOf("nullScenario", "aValue").shouldBeNull()
        accessor.valueOf("scenarioDoesNotExist", "foo").shouldBeNull()
    }

    @Test
    internal fun `caches return values`() {
        accessor.valueOf("scenario", "aValue") shouldBe "FooBoo!!"
        accessor.valueOf("scenario", "aValue") shouldBe "FooBoo!!"
    }
}