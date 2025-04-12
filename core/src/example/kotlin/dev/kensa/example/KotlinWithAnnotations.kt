package dev.kensa.example

import dev.kensa.*
import dev.kensa.DummyAssert.Companion.assertThat
import dev.kensa.SomeBuilder.Companion.someBuilder

class KotlinWithAnnotations {

    @field:[Highlight SentenceValue]
    private val field1: String? = null

    @[Highlight SentenceValue]
    private val property1: String = "property1"

    @get:[Highlight SentenceValue]
    private val propertyWithGetter: String
        get() = "propertyWithGetter"

    @get:[SentenceValue]
    private val throwingGetter: String
        get() = throw RuntimeException("Boom!")

    @get:[Highlight]
    private val lazyProperty: String by lazy { "lazyProperty" }

    fun simpleTest() {
        assertThat("true").isEqualTo("true")
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
    internal fun internalNested() {
    }

    companion object {
        const val MY_PARAMETER_VALUE: String = "myParameterValue"
    }
}