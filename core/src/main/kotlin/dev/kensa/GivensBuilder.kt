package dev.kensa

import dev.kensa.state.Givens
import kotlin.DeprecationLevel.WARNING

@Deprecated("use Action<GivensBuilder> instead", ReplaceWith("Action<GivensContext>", "dev.kensa.Action", "dev.kensa.GivensContext"), WARNING)
@FunctionalInterface
fun interface GivensBuilder {
    fun build(givens: Givens)
}