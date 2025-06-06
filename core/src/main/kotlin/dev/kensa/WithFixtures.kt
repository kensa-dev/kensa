package dev.kensa

import dev.kensa.context.TestContextHolder
import dev.kensa.fixture.Fixtures

interface WithFixtures {
    val fixtures: Fixtures
}

object TestContextFixtures : WithFixtures {
    override val fixtures: Fixtures = TestContextHolder.testContext().fixtures
}
