package dev.kensa

import dev.kensa.fixture.Fixtures
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens
import kotlin.DeprecationLevel.WARNING

@Deprecated("use Action<ActionContext> instead", ReplaceWith("Action<ActionContext>", "dev.kensa.Action", "dev.kensa.ActionContext"), WARNING)
@FunctionalInterface
fun interface ActionUnderTest {
    fun execute(givens: Givens, interactions: CapturedInteractions)
}

@Deprecated("use Action<ActionContext> instead", ReplaceWith("Action<ActionContext>", "dev.kensa.Action", "dev.kensa.ActionContext"), WARNING)
@FunctionalInterface
fun interface ActionUnderTestWithFixtures {
    fun execute(givens: Givens, fixtures: Fixtures, interactions: CapturedInteractions)
}