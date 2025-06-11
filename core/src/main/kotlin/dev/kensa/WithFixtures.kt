package dev.kensa

import dev.kensa.context.TestContextHolder
import dev.kensa.fixture.Fixtures

interface WithFixtures {
    val fixtures: Fixtures
}

class TestContextFixtures : WithFixtures {
    override val fixtures: Fixtures get() = TestContextHolder.testContext().fixtures
}