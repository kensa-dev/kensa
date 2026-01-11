package dev.kensa

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

    @Highlight
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

    infix fun String.then(expectedValue: String) : String{
        return expectedValue
    }

    @RenderedValue
    fun renderTheReturnValue(one: String, two: String): String = "$one-$two"

    @RenderedValue
    private fun aSimpleSentenceValueFunction() = "myValue"

    @RenderedValue
    private fun aSentenceValueFunction() = MyThing("myValue")

    private fun somethingWith(value: String): GivensBuilder = GivensBuilder { }

    private fun theExtractedValue(): StateExtractor<String?> = StateExtractor { interactions: CapturedInteractions -> aValue }

    private fun somePrerequisites(): GivensBuilder = GivensBuilder { givens: Givens -> givens.put("foo", "bar") }

    private fun somePrerequisitesWith(vararg values: String): GivensBuilder = GivensBuilder { givens: Givens ->
        values.forEach { givens.put("key_$it", it) }
    }

    private fun someAction(): ActionUnderTest = ActionUnderTest { _, _ -> }
    private fun someActionWith(value: String): ActionUnderTest = ActionUnderTest { _, _ -> }

    @Emphasise(textStyles = [TextWeightBold, Italic, Uppercase], textColour = TextLight, backgroundColor = BackgroundDanger)
    private fun someActionWithEmphasis(): ActionUnderTest = ActionUnderTest { _, _ -> }

    companion object {
        @JvmStatic
        fun myThing() = listOf(MyThing("foo"), MyThing("bar"))
    }
}

data class MyThing(val value: String)

