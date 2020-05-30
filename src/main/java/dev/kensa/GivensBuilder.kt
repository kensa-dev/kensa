package dev.kensa

import dev.kensa.state.Givens

@FunctionalInterface
interface GivensBuilder {
    fun build(givens: Givens)
}