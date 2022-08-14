package dev.kensa

import dev.kensa.Colour.BackgroundDanger
import dev.kensa.Colour.TextLight
import dev.kensa.AssertionStyleTest.Parrty.A
import dev.kensa.AssertionStyleTest.Parrty.B
import dev.kensa.TextStyle.*
import dev.kensa.kotlin.KotlinKensaTest
import dev.kensa.kotlin.WithKotest
import dev.kensa.render.diagram.directive.ArrowStyle.UmlAsynchronousDelete
import dev.kensa.state.CapturedInteractionBuilder
import io.kotest.matchers.*
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.startWith
import org.junit.jupiter.api.Test

class KotlinTestWithKotest : KotlinKensaTest, WithKotest {

    private val actionName = "ACTION1"

    @SentenceValue
    private val theExpectedResult = "Performed: ACTION1"

    private val performer = ActionPerformer()

    @Test
    fun expressionTest() {
        given(someActionNameIsAddedToGivens())

        whenever(theActionIsPerformedAndTheResultIsAddedToCapturedInteractions())

        then(theResultStoredInCapturedInteractions()) {
            shouldNotBeNull()
            shouldBe(theExpectedResult)
            should(be(theExpectedResult).and(startWith("Per")))
        }

        then(theResultStoredInCapturedInteractions(), be(theExpectedResult))
    }

    @Emphasise(textStyles = [TextWeightBold, Italic, Uppercase, TextDecorationUnderline], textColour = TextLight, backgroundColor = BackgroundDanger)
    fun theActionIsPerformedAndTheResultIsAddedToCapturedInteractions(): ActionUnderTest {
        return ActionUnderTest { givens, interactions ->
            interactions.capture(
                CapturedInteractionBuilder.from(A).to(B).group("Test").arrowStyle(UmlAsynchronousDelete).with("Message", "The Message"))
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