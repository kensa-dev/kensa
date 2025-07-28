package dev.kensa

import dev.kensa.context.TestContextHolder
import dev.kensa.fixture.Fixture
import dev.kensa.outputs.CapturedOutput
import dev.kensa.outputs.CapturedOutputs

interface WithFixturesAndOutputs {
    val fixturesAndOutputs: FixturesAndOutputs
    val outputs: CapturedOutputs get() = fixturesAndOutputs.outputs
    val fixtures get() = fixturesAndOutputs.fixtures

    fun <T : Any> outputs(key: String) = fixturesAndOutputs.outputs[key] as? T
    fun <T : Any> outputs(name: CapturedOutput<T>) = fixturesAndOutputs.outputs[name]
    fun <T> fixtures(fixture: Fixture<T>) = fixtures[fixture]
}

class TestContextFixturesAndOutputs : WithFixturesAndOutputs {
    override val fixturesAndOutputs: FixturesAndOutputs get() = TestContextHolder.testContext().fixturesAndOutputs
}