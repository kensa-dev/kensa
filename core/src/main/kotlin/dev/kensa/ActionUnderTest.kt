package dev.kensa

import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens
import kotlin.DeprecationLevel.WARNING

@Deprecated("use Action<ActionContext> instead", ReplaceWith("Action<ActionContext>", "dev.kensa.Action", "dev.kensa.ActionContext"), WARNING)
@FunctionalInterface
fun interface ActionUnderTest {
    fun execute(givens: Givens, interactions: CapturedInteractions)
}