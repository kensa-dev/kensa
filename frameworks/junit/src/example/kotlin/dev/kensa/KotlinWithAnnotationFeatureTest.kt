package dev.kensa

import com.natpryce.hamkrest.equalTo
import dev.kensa.Colour.BackgroundDanger
import dev.kensa.Colour.TextLight
import dev.kensa.Kensa.configure
import dev.kensa.TextStyle.Italic
import dev.kensa.TextStyle.TextWeightBold
import dev.kensa.TextStyle.Uppercase
import dev.kensa.fixture.MyScenario
import dev.kensa.fixture.MyScenarioHolder
import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.junit.KensaTest
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class KotlinWithAnnotationFeatureTest : KensaTest, WithHamkrest {

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

    @BeforeEach
    fun setUp() {
        configure()
            .withValueRenderer(MyThing::class.java) {
                """MyThing"""
            }
    }

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
        given(someSomethingWith(aSimpleSentenceValueFunction()))
    }

    @Test
    fun testWithChainedFunctionSentenceValue() {
        given(someSomethingWith(aSentenceValueFunction().value))
    }

    @Test
    fun testWithChainedFieldSentenceValue() {
        given(someSomethingWith(myThing.value))
    }

    @ParameterizedTest
    @MethodSource("myThing")
    fun testWithChainedFunctionSentenceValueParameter(@RenderedValue value: MyThing) {
        given(someSomethingWith(value.value))
    }

    @RenderedValue
    private fun aSimpleSentenceValueFunction() = "myValue"

    @RenderedValue
    private fun aSentenceValueFunction() = MyThing("myValue")

    private fun someSomethingWith(value: String): GivensBuilderWithFixtures = GivensBuilderWithFixtures { _, _ -> }

    private fun theExtractedValue(): StateExtractor<String?> = StateExtractor { interactions: CapturedInteractions -> aValue }

    private fun somePrerequisites(): GivensBuilderWithFixtures = GivensBuilderWithFixtures { givens: Givens, _ -> givens.put("foo", "bar") }

    private fun somePrerequisitesWith(vararg values: String): GivensBuilderWithFixtures = GivensBuilderWithFixtures { givens: Givens, _ ->
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

