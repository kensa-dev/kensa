package dev.kensa

import dev.kensa.kotlin.KotlinKensaTest
import dev.kensa.kotlin.WithKotest
import dev.kensa.render.diagram.directive.ArrowStyle
import dev.kensa.state.CapturedInteractionBuilder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class KotlinTestWithKotestAssertions : KotlinKensaTest, WithKotest {

    private val actionName = "ACTION1"

    @SentenceValue
    private val theExpectedResult = "Performed: ACTION1"

    private val performer = ActionPerformer()

    @Test
    fun expressionTest() {
        given(someActionNameIsAddedToGivens())

        whenever(theActionIsPerformedAndTheResultIsAddedToCapturedInteractions())

        then(theResultStoredInCapturedInteractions()) {
            shouldBe(theExpectedResult)
        }
    }

    @Emphasise(textStyles = [TextStyle.TextWeightBold, TextStyle.Italic, TextStyle.Uppercase, TextStyle.TextDecorationUnderline], textColour = Colour.TextLight, backgroundColor = Colour.BackgroundDanger)
    fun theActionIsPerformedAndTheResultIsAddedToCapturedInteractions(): ActionUnderTest {
        return ActionUnderTest { givens, interactions ->
            interactions.capture(
                CapturedInteractionBuilder.from(KAssertionStyleTest.Parrty.A).to(KAssertionStyleTest.Parrty.B).group("Test").arrowStyle(ArrowStyle.UmlAsynchronousDelete).with("Message", "The Message"))
            givens.get<String>("actionName")?.let {
                interactions.put("result", performer.perform(it))
            }
        }
    }

    private fun theResultStoredInCapturedInteractions(): StateExtractor<String?> {
        return StateExtractor { interactions -> interactions["result"] }
    }

    private fun someActionNameIsAddedToGivens() =
        GivensBuilder { givens -> givens.put("actionName", actionName) }
}