package dev.kensa

import dev.kensa.fixture.Fixtures
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens

@FunctionalInterface
fun interface ActionUnderTest {
    fun execute(givens: Givens, interactions: CapturedInteractions)
}

@FunctionalInterface
fun interface ActionUnderTestWithFixtures {
    fun execute(givens: Givens, fixtures: Fixtures, interactions: CapturedInteractions)
}