package dev.kensa

import dev.kensa.context.TestContextHolder
import dev.kensa.fixture.Fixture
import dev.kensa.fixture.Fixtures
import dev.kensa.fixture.PrimaryFixture

interface WithFixtures {
    val fixtures: Fixtures
    fun <T> fixtures(fixture: Fixture<T>)= fixtures[fixture]
    operator fun <T> set(fixture: PrimaryFixture<T>, value: T) = fixtures.set(fixture, value)
}

class TestContextFixtures : WithFixtures {
    override val fixtures: Fixtures get() = TestContextHolder.testContext().fixtures
}