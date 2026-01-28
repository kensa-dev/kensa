package dev.kensa.example

import dev.kensa.DummyAssert.Companion.assertThat
import dev.kensa.ExpandableSentence

class KotlinWithParameters {

    fun parameterizedTest(first: String?, second: Int?) {
        assertThat(first).isIn("a", "b")
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
}