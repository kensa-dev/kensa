package dev.kensa

import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens

@FunctionalInterface
fun interface ActionUnderTest {
    fun execute(givens: Givens, interactions: CapturedInteractions)
}