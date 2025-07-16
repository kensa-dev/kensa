package dev.kensa.state

import dev.kensa.FixturesAndOutputs
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.util.isKotlinClass
import java.lang.reflect.Method

class TestInvocationContext(val instance: Any, val method: Method, val arguments: Array<Any?>, val displayName: String, val startTimeMs: Long, val fixtures: Fixtures, val capturedOutputs: CapturedOutputs) {
    val fixturesAndOutputs = FixturesAndOutputs(fixtures, capturedOutputs)
    val isKotlin = instance::class.isKotlinClass
}