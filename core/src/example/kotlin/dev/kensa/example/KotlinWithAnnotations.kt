package dev.kensa.example

import dev.kensa.DummyAssert.Companion.assertThat
import dev.kensa.ExpandableSentence
import dev.kensa.GivensBuilder
import dev.kensa.Highlight
import dev.kensa.RenderedValue
import dev.kensa.SomeBuilder.Companion.someBuilder

class KotlinWithAnnotations {

    @field:[Highlight RenderedValue]
    private val field1: String? = null

    @[Highlight RenderedValue]
    private val property1: String = "property1"

    @get:[Highlight RenderedValue]
    private val propertyWithGetter: String
        get() = "propertyWithGetter"

    @get:[RenderedValue]
    private val throwingGetter: String
        get() = throw RuntimeException("Boom!")

    @get:[Highlight]
    private val lazyProperty: String by lazy { "lazyProperty" }

    fun simpleTest() {
        assertThat("true").isEqualTo("true")
    }

    @RenderedValue
    fun method1() {
    }

    @ExpandableSentence
    private fun nested1(): GivensBuilder {
        return someBuilder()
            .withSomething()
            .build()
    }

    @ExpandableSentence
    internal fun internalNested() {
    }

    companion object {
        const val MY_PARAMETER_VALUE: String = "myParameterValue"
    }
}