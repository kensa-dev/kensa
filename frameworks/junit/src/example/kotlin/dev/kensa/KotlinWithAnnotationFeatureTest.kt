package dev.kensa

import com.natpryce.hamkrest.equalTo
import dev.kensa.Colour.BackgroundDanger
import dev.kensa.Colour.TextLight
import dev.kensa.TextStyle.Italic
import dev.kensa.TextStyle.TextWeightBold
import dev.kensa.TextStyle.Uppercase
import dev.kensa.fixture.MyScenario
import dev.kensa.fixture.MyScenarioHolder
import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.junit.KensaTest
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens
import org.junit.jupiter.api.Test

class KotlinWithAnnotationFeatureTest : KensaTest, WithHamkrest {

    @Highlight
    private val highlightMe = "givensViaHighlight"

    @SentenceValue
    private val aValue = "aStringValue"

    @Scenario
    private val myScenario = MyScenario(aValue)

    @ScenarioHolder
    private val myHolder = MyScenarioHolder(myScenario)

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

    private fun theExtractedValue(): StateExtractor<String?> = StateExtractor { interactions: CapturedInteractions -> aValue }

    private fun somePrerequisites(): GivensBuilder = GivensBuilder { givens: Givens -> givens.put("foo", "bar") }

    private fun somePrerequisitesWith(vararg values: String): GivensBuilder = GivensBuilder { givens: Givens ->
        values.forEach { givens.put("key_$it", it) }
    }

    private fun someAction(): ActionUnderTest = ActionUnderTest { givens: Givens, interactions: CapturedInteractions? -> }
    private fun someActionWith(value: String): ActionUnderTest = ActionUnderTest { givens: Givens, interactions: CapturedInteractions? -> }

    @Emphasise(textStyles = [TextWeightBold, Italic, Uppercase], textColour = TextLight, backgroundColor = BackgroundDanger)
    private fun someActionWithEmphasis(): ActionUnderTest = ActionUnderTest { givens: Givens, interactions: CapturedInteractions? -> }
}

