package dev.kensa

import dev.kensa.fixture.Fixtures
import dev.kensa.state.CapturedInteractions

@FunctionalInterface
fun interface StateExtractor<T> {
    fun execute(interactions: CapturedInteractions): T
}

@FunctionalInterface
fun interface StateExtractorWithFixtures<T> {
    fun execute(fixtures: Fixtures, interactions: CapturedInteractions): T
}