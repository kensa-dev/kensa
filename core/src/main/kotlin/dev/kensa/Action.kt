package dev.kensa

import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.state.CapturedInteractions

/**
 * Holds the components optionally used to perform test setup via the `given(Action<GivensContext>)`.
 *
 * @property fixtures The fixtures available for test setup.
 * @property outputs The outputs available for capturing during test setup.
 */
data class GivensContext(val fixtures: Fixtures, val outputs: CapturedOutputs) {
    val fixturesAndOutputs = FixturesAndOutputs(fixtures, outputs)
}

/**
 * Holds the components optionally used to perform the action execution via `Action<ActionContext>`.
 *
 * @property fixtures The fixtures available during the action execution.
 * @property interactions The captured interactions available during the action execution.
 * @property outputs The outputs available for capturing during the action execution.
 */
data class ActionContext(val fixtures: Fixtures, val interactions: CapturedInteractions, val outputs: CapturedOutputs) {
    val fixturesAndOutputs = FixturesAndOutputs(fixtures, outputs)
}

/**
 * Holds the components optionally used for state collection via `StateCollector`.
 *
 * @property fixtures The fixtures available during state collection.
 * @property interactions The captured interactions available during state collection.
 * @property outputs The outputs available for capturing during state collection.
 */
data class CollectorContext(val fixtures: Fixtures, val interactions: CapturedInteractions, val outputs: CapturedOutputs) {
    val fixturesAndOutputs = FixturesAndOutputs(fixtures, outputs)
}

/**
 * A wrapper for fixtures and outputs, as these two components are often used together in extension functions.
 *
 * @property fixtures The fixtures available.
 * @property outputs The outputs available.
 */
data class FixturesAndOutputs(val fixtures: Fixtures, val outputs: CapturedOutputs)

/**
 * Functional interface representing an action executed with a given context.
 *
 * @param C The type of context required for the action.
 */
@FunctionalInterface
fun interface Action<C> {
    /**
     * Executes the action with the given context.
     *
     * @param context The context containing the relevant data for the action.
     */
    fun execute(context: C)
}