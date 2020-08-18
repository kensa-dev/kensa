package dev.kensa

import dev.kensa.state.CapturedInteractions

@FunctionalInterface
fun interface StateExtractor<T> {
    fun execute(interactions: CapturedInteractions): T?
}