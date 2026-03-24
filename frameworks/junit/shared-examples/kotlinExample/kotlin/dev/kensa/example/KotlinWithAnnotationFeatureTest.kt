package dev.kensa.example

import com.natpryce.hamkrest.equalTo
import dev.kensa.*
import dev.kensa.Colour.BackgroundDanger
import dev.kensa.Colour.TextLight
import dev.kensa.TextStyle.*
import dev.kensa.fixture.MyScenario
import dev.kensa.fixture.MyScenarioHolder
import dev.kensa.hamkrest.WithHamkrest
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

class KotlinWithAnnotationFeatureTest : KotlinExampleTest(), WithHamkrest {

    @dev.kensa.Highlight
    private val highlightMe = "givensViaHighlight"

    @RenderedValue
    private val aValue = "aStringValue"

    @RenderedValue
    private val myScenario = MyScenario(aValue)

    @RenderedValueContainer
    private val myHolder = MyScenarioHolder(myScenario)

    @RenderedValue
    private val myThing = MyThing("myFieldThing")

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
    fun testWithChainedFunctionSentenceValueParameter(@RenderedValue value: MyThing) {
        given(somethingWith(value.value))
    }

    @Test
    fun testWithRenderedValueFunctionWithParameters() {
        given(somethingWith(renderTheReturnValue("foo", "bar")))
    }

    @ParameterizedTest
    @ValueSource(strings = ["meh"])
    fun parameterisedTestWithRenderedValueFunctionWithParameters(@RenderedValue shouldNotBeRendered: String) {
        given(somethingWith(renderTheReturnValue(shouldNotBeRendered, "bar")))
    }

    @Test
    fun testWithInfixStyleRenderedValueFunctionWithParameters() {
        given(somethingWith("prefix" then renderTheReturnValue("foo", "bar")))
    }

    infix fun String.then(expectedValue: String): String {
        return expectedValue
    }

    @RenderedValue
    fun renderTheReturnValue(one: String, two: String): String = "$one-$two"

    @RenderedValue
    private fun aSimpleSentenceValueFunction() = "myValue"

    @RenderedValue
    private fun aSentenceValueFunction() = MyThing("myValue")

    private fun somethingWith(value: String) = Action<GivensContext> {}

    private fun theExtractedValue() = StateCollector { _ -> aValue }

    private fun somePrerequisites() = Action<GivensContext> {}

    private fun somePrerequisitesWith(vararg values: String) = Action<GivensContext> {}

    private fun someAction() = Action<ActionContext> {}
    private fun someActionWith(value: String) = Action<ActionContext> {}

    @dev.kensa.Emphasise(textStyles = [TextWeightBold, Italic, Uppercase], textColour = TextLight, backgroundColor = BackgroundDanger)
    private fun someActionWithEmphasis() = Action<ActionContext> {}

    companion object {
        @JvmStatic
        fun myThing() = listOf(MyThing("foo"), MyThing("bar"))
    }
}

data class MyThing(val value: String)

