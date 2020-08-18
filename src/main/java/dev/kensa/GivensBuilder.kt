package dev.kensa

import dev.kensa.state.Givens

@FunctionalInterface
fun interface GivensBuilder {
    fun build(givens: Givens)
}