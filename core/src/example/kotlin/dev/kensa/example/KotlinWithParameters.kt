package dev.kensa.example

import dev.kensa.DummyAssert.Companion.assertThat
import dev.kensa.ExpandableSentence
import dev.kensa.RenderedValue

class KotlinWithParameters {

    class Actor {
        class Scenario
    }

    fun parameterizedTest(first: String?, second: Int?) {
        assertThat(first).isIn("a", "b")
    }

    fun varargTest(vararg first: String) {
        assertThat(first.first()).isIn("a", "b")
    }

    fun `perform line diagnostics check when there is no active session`(
        @RenderedValue sessionCheckScenario: Actor.Scenario,
        @RenderedValue currentSpeedLineDownstream: String,
        @RenderedValue dailyMaxCurrentRates: Array<Pair<String, String>>,
        @Suppress("UNUSED")
        @RenderedValue description: String
    ) {
        assertThat(dailyMaxCurrentRates.first().first).isIn("a", "b")
    }

    @ExpandableSentence
    fun String.anExtensionFunction(first: String?, second: Int?) {
        assertThat(first).isIn("a", "b")
    }

    context(intParam: Int)
    @ExpandableSentence
    fun aFunctionWithContextParametersBeforeFn(first: String?, second: Int?) {
        assertThat(first).isIn("a", "b")
    }

    @ExpandableSentence
    context(intParam: Int)
    fun aFunctionWithContextParametersBeforeContext(first: String?, second: Int?) {
        assertThat(first).isIn("a", "b")
    }

    @ExpandableSentence
    context(intParam: Int)
    fun String.anExtensionFunctionWithContextParameters(first: String?, second: Int?) {
        assertThat(first).isIn("a", "b")
    }

    @ExpandableSentence
    fun aFunctionWithNullableFloat(first: Float?) {
        assertThat(first?.toString()).isIn("a", "b")
    }
}