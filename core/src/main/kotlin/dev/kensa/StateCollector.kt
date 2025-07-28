package dev.kensa

import dev.kensa.state.CapturedInteractions
import kotlin.DeprecationLevel.WARNING

@Deprecated("use StateCollector<T> instead", ReplaceWith("StateCollector<T>", "dev.kensa.StateCollector"), WARNING)
@FunctionalInterface
fun interface StateExtractor<T> {
    fun execute(interactions: CapturedInteractions): T
}

@FunctionalInterface
fun interface StateCollector<T> {
    fun execute(context: CollectorContext): T
}