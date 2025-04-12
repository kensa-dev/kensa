package dev.kensa.example

import dev.kensa.*
import dev.kensa.DummyAssert.Companion.assertThat
import dev.kensa.SomeBuilder.Companion.someBuilder

class KotlinWithParameters {
    private val field1: String? = null

    @field:Scenario
    private val field2: String? = null

    @field:[Highlight SentenceValue]
    private val field3: String? = null

    @[Highlight SentenceValue]
    private val property1: String = "property1"

    @get:[Highlight SentenceValue]
    private val propertyWithGetter: String
        get() = "propertyWithGetter"

    @get:[Highlight SentenceValue]
    private val throwingGetter: String
        get() = throw RuntimeException("Boom!")

    @get:[Highlight SentenceValue]
    private val lazyProperty: String by lazy { "lazyProperty" }

    fun simpleTest() {
        assertThat("true").isEqualTo("true")
    }

    internal fun internalTest() {
        assertThat("true").isEqualTo("true")
    }

    fun parameterizedTest(first: String?) {
        assertThat(first).isIn("a", "b")
    }

    @SentenceValue
    fun method1() {
    }

    @NestedSentence
    private fun nested1(): GivensBuilder {
        return someBuilder()
            .withSomething()
            .build()
    }

    @NestedSentence
    internal fun nested123() {
    }

    companion object {
        const val MY_PARAMETER_VALUE: String = "myParameterValue"
    }
}