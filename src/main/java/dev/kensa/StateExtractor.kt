package dev.kensa

import dev.kensa.state.CapturedInteractions

@FunctionalInterface
interface StateExtractor<T> {
    fun execute(interactions: CapturedInteractions): T?
}