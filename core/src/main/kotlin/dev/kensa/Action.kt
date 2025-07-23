package dev.kensa

import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.state.CapturedInteractions

data class GivensContext(val fixtures: Fixtures, val outputs: CapturedOutputs) {
    val fixturesAndOutputs = FixturesAndOutputs(fixtures, outputs)
}
data class ActionContext(val fixtures: Fixtures, val interactions: CapturedInteractions, val outputs: CapturedOutputs) {
    val fixturesAndOutputs = FixturesAndOutputs(fixtures, outputs)
}
data class CollectorContext(val fixtures: Fixtures, val interactions: CapturedInteractions, val outputs: CapturedOutputs)
data class FixturesAndOutputs(val fixtures: Fixtures, val outputs: CapturedOutputs)

@FunctionalInterface
fun interface Action<C> {
    fun execute(context: C)
}
