package dev.kensa

import dev.kensa.fixture.Fixtures
import dev.kensa.state.CapturedInteractions
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.state.Givens
import kotlin.DeprecationLevel.WARNING

data class GivensContext(val fixtures: Fixtures)
data class ActionContext(val fixtures: Fixtures, val interactions: CapturedInteractions, val outputs: CapturedOutputs) {
    val fixturesAndOutputs = FixturesAndOutputs(fixtures, outputs)
}
data class CollectorContext(val fixtures: Fixtures, val interactions: CapturedInteractions, val outputs: CapturedOutputs)
data class FixturesAndOutputs(val fixtures: Fixtures, val outputs: CapturedOutputs)

@FunctionalInterface
fun interface Action<C> {
    fun execute(context: C)
}

@Deprecated("use Action instead", ReplaceWith("action"), WARNING)
@FunctionalInterface
fun interface ActionUnderTest {
    fun execute(givens: Givens, interactions: CapturedInteractions)
}

@Deprecated("use Action instead", ReplaceWith("action"), WARNING)
@FunctionalInterface
fun interface ActionUnderTestWithFixtures {
    fun execute(givens: Givens, fixtures: Fixtures, interactions: CapturedInteractions)
}