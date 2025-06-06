package dev.kensa

import dev.kensa.fixture.Fixtures
import dev.kensa.state.Givens

@FunctionalInterface
fun interface GivensBuilder {
    fun build(givens: Givens)
}

@FunctionalInterface
fun interface GivensBuilderWithFixtures {
    fun build(givens: Givens, fixtures: Fixtures)
}