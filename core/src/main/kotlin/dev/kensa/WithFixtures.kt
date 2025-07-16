package dev.kensa

import dev.kensa.context.TestContextHolder
import dev.kensa.fixture.Fixture
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutput
import dev.kensa.outputs.CapturedOutputs

interface WithFixturesAndOutputs : WithFixtures {
    val fixturesAndOutputs: FixturesAndOutputs
    val outputs: CapturedOutputs get() = fixturesAndOutputs.outputs
    override val fixtures get() = fixturesAndOutputs.fixtures

    fun <T : Any> outputs(output: CapturedOutput<T>) = fixturesAndOutputs.outputs[output]
    override fun <T> fixtures(fixture: Fixture<T>) = fixtures[fixture]
}

@Deprecated("Use WithFixturesAndOutputs instead", ReplaceWith("WithFixturesAndOutputs"))
interface WithFixtures {
    val fixtures: Fixtures
    fun <T> fixtures(fixture: Fixture<T>) = fixtures[fixture]
}

class TestContextFixtures : WithFixtures {
    override val fixtures: Fixtures get() = TestContextHolder.testContext().fixtures
}

class TestContextFixturesAndOutputs : WithFixturesAndOutputs {
    override val fixturesAndOutputs: FixturesAndOutputs get() = TestContextHolder.testContext().fixturesAndOutputs
}