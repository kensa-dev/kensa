package dev.kensa.example

import com.natpryce.hamkrest.equalTo
import dev.kensa.Colour.BackgroundDanger
import dev.kensa.Colour.TextLight
import dev.kensa.TextStyle.*
import dev.kensa.fixture.MyScenario
import dev.kensa.fixture.MyScenarioHolder
import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

class KotlinWithAnnotationFeatureTest : KotlinExampleTest(), WithHamkrest {

    @dev.kensa.Highlight
    private val highlightMe = "givensViaHighlight"

    @dev.kensa.RenderedValue
    private val aValue = "aStringValue"

    @dev.kensa.RenderedValue
    private val myScenario = MyScenario(aValue)

    @dev.kensa.RenderedValueContainer
    private val myHolder = MyScenarioHolder(myScenario)

    @dev.kensa.RenderedValue
    private val myThing = _root_ide_package_.dev.kensa.example.MyThing("myFieldThing")

    @Test
    fun testWithScenario() {
        given(somePrerequisites())

        whenever(someAction())

        then(theExtractedValue(), equalTo(myScenario.stringValue))
    }

    @Test
    fun testWithScenarioHolder() = with(myHolder) {
        given(somePrerequisites())

        whenever(someActionWith(highlightMe))

        then(theExtractedValue(), equalTo(scenario.stringValue))
    }

    @Test
    fun testWithHighlight() {
        given(somePrerequisitesWith("noHighlight"))

        whenever(someActionWith(highlightMe))

        then(theExtractedValue(), equalTo(aValue))
    }

    @Test
    fun testWithEmphasis() {
        whenever(someActionWithEmphasis())

        then(theExtractedValue(), equalTo(aValue))
    }

    @Test
    fun testWithSimpleFunctionSentenceValue() {
        given(somethingWith(aSimpleSentenceValueFunction()))
    }

    @Test
    fun testWithChainedFunctionSentenceValue() {
        given(somethingWith(aSentenceValueFunction().value))
    }

    @Test
    fun testWithChainedFieldSentenceValue() {
        given(somethingWith(myThing.value))
    }

    @ParameterizedTest
    @MethodSource("myThing")
    fun testWithChainedFunctionSentenceValueParameter(@dev.kensa.RenderedValue value: MyThing) {
        given(somethingWith(value.value))
    }

    @Test
    fun testWithRenderedValueFunctionWithParameters() {
        given(somethingWith(renderTheReturnValue("foo", "bar")))
    }

    @ParameterizedTest
    @ValueSource(strings = ["meh"])
    fun parameterisedTestWithRenderedValueFunctionWithParameters(@dev.kensa.RenderedValue shouldNotBeRendered: String) {
        given(somethingWith(renderTheReturnValue(shouldNotBeRendered, "bar")))
    }

    @Test
    fun testWithInfixStyleRenderedValueFunctionWithParameters() {
        given(somethingWith("prefix" then renderTheReturnValue("foo", "bar")))
    }

    infix fun String.then(expectedValue: String) : String{
        return expectedValue
    }

    @dev.kensa.RenderedValue
    fun renderTheReturnValue(one: String, two: String): String = "$one-$two"

    @dev.kensa.RenderedValue
    private fun aSimpleSentenceValueFunction() = "myValue"

    @dev.kensa.RenderedValue
    private fun aSentenceValueFunction() = _root_ide_package_.dev.kensa.example.MyThing("myValue")

    private fun somethingWith(value: String): dev.kensa.GivensBuilder = _root_ide_package_.dev.kensa.GivensBuilder { }

    private fun theExtractedValue(): dev.kensa.StateExtractor<String?> = _root_ide_package_.dev.kensa.StateExtractor { interactions: CapturedInteractions -> aValue }

    private fun somePrerequisites(): dev.kensa.GivensBuilder = _root_ide_package_.dev.kensa.GivensBuilder { givens: Givens -> givens.put("foo", "bar") }

    private fun somePrerequisitesWith(vararg values: String): dev.kensa.GivensBuilder = _root_ide_package_.dev.kensa.GivensBuilder { givens: Givens ->
        values.forEach { givens.put("key_$it", it) }
    }

    private fun someAction(): dev.kensa.ActionUnderTest = _root_ide_package_.dev.kensa.ActionUnderTest { _, _ -> }
    private fun someActionWith(value: String): dev.kensa.ActionUnderTest = _root_ide_package_.dev.kensa.ActionUnderTest { _, _ -> }

    @dev.kensa.Emphasise(textStyles = [TextWeightBold, Italic, Uppercase], textColour = TextLight, backgroundColor = BackgroundDanger)
    private fun someActionWithEmphasis(): dev.kensa.ActionUnderTest = _root_ide_package_.dev.kensa.ActionUnderTest { _, _ -> }

    companion object {
        @JvmStatic
        fun myThing() = listOf(_root_ide_package_.dev.kensa.example.MyThing("foo"), _root_ide_package_.dev.kensa.example.MyThing("bar"))
    }
}

data class MyThing(val value: String)

