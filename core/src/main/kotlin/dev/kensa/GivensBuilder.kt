package dev.kensa

import dev.kensa.state.Givens
import kotlin.DeprecationLevel.WARNING

@Deprecated("use Action instead", ReplaceWith("action"), WARNING)
@FunctionalInterface
fun interface GivensBuilder {
    fun build(givens: Givens)
}