package dev.kensa

import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens

@FunctionalInterface
fun interface GivensWithInteractionsBuilder {
    fun build(givens: Givens, capturedInteractions: CapturedInteractions)
}