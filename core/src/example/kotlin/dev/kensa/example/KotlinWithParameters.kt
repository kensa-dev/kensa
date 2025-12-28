package dev.kensa.example

import dev.kensa.DummyAssert.Companion.assertThat
import dev.kensa.NestedSentence

class KotlinWithParameters {

    fun parameterizedTest(first: String?, second: Int?) {
        assertThat(first).isIn("a", "b")
    }

    @NestedSentence
    fun String.anExtensionFunction(first: String?, second: Int?) {
        assertThat(first).isIn("a", "b")
    }

    context(intParam: Int)
    @NestedSentence
    fun aFunctionWithContextParametersBeforeFn(first: String?, second: Int?) {
        assertThat(first).isIn("a", "b")
    }

    @NestedSentence
    context(intParam: Int)
    fun aFunctionWithContextParametersBeforeContext(first: String?, second: Int?) {
        assertThat(first).isIn("a", "b")
    }

    @NestedSentence
    context(intParam: Int)
    fun String.anExtensionFunctionWithContextParameters(first: String?, second: Int?) {
        assertThat(first).isIn("a", "b")
    }
}