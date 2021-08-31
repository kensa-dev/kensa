package dev.kensa

import dev.kensa.Colour.BackgroundDanger
import dev.kensa.Colour.TextLight
import dev.kensa.Kensa.konfigure
import dev.kensa.Section.*
import dev.kensa.TextStyle.*
import dev.kensa.kotlin.KotlinKensaTest
import dev.kensa.kotlin.WithAssertJ
import dev.kensa.kotlin.WithHamcrest
import dev.kensa.render.diagram.directive.ArrowStyle
import dev.kensa.render.diagram.directive.UmlParticipant.Companion.actor
import dev.kensa.sentence.Acronym
import dev.kensa.state.CapturedInteractionBuilder.Companion.from
import dev.kensa.state.Party
import org.antlr.v4.runtime.atn.PredictionMode
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@Notes("Class Level Notes")
@Issue("ISS-007")
class KAssertionStyleTest : KotlinKensaTest, WithAssertJ, WithHamcrest {
    private val actionName = "ACTION1"

    @SentenceValue
    private val theExpectedResult = "Performed: ACTION1"

    @Scenario
    private val scenario = ScenarioFoo()

    private val performer = ActionPerformer()

    @BeforeEach
    fun setUp() {
        konfigure {
            keywords = setOf("that")
            acronyms = setOf(Acronym.of("ACTION1", ""))
            antlrPredicationMode = PredictionMode.LL
            sectionOrder = listOf(Sentences, Buttons, Exception)
            umlDirectives = listOf(actor("A"), actor("B"))
        }
    }

    enum class Parrty : Party {
        A,
        B;

        override fun asString(): String = this.name
    }

    @Test
    @Notes("Method Notes {@link AssertionStyleTest#canUseAssertJStyle}")
    @Issue("ISS-007")
    fun `can Use AssertJStyle`() {
        given(someActionNameIsAddedToGivens())

        whenever(theActionIsPerformedAndTheResultIsAddedToCapturedInteractions())

        then(theResultStoredInCapturedInteractions()).isEqualTo(theExpectedResult)
        withAllTheNestedThings()
        then(foo()).isEqualTo(scenario.thing())
        then(foo1())
                .isEqualTo("777")
                .hasSameClassAs("888")

        then(foo())
                .isEqualTo(666)
    }

    @NestedSentence
    private fun withAllTheNestedThings() {
        then(foo()).isEqualTo(scenario.thing())
    }

    private fun foo(): StateExtractor<Int?> {
        return StateExtractor {
            it.divider()
            666
        }
    }

    private fun foo1(): StateExtractor<String?> {
        return StateExtractor { "777" }
    }

    @Test
    fun canUseHamcrestStyle() {
        given(someActionNameIsAddedToGivens())
        whenever(theActionIsPerformedAndTheResultIsAddedToCapturedInteractions())
        then(theResultStoredInCapturedInteractions())
    }

    @ParameterizedTest
    @MethodSource("parameterProvider")
    fun parameterizedTest(@SentenceValue actionName: String, @SentenceValue theExpectedResult: String) {
        given(somethingIsDoneWith(actionName))
        whenever(theActionIsPerformedAndTheResultIsAddedToCapturedInteractions())
        then(theResultStoredInCapturedInteractions(), `is`(theExpectedResult))
    }

    private fun somethingIsDoneWith(actionName: String): GivensBuilder {
        return GivensBuilder { givens -> givens.put("actionName", actionName) }
    }

    private fun someActionNameIsAddedToGivens(): GivensBuilder {
        return GivensBuilder { givens -> givens.put("actionName", actionName) }
    }

    @Emphasise(textStyles = [TextWeightBold, Italic, Uppercase, TextDecorationUnderline], textColour = TextLight, backgroundColor = BackgroundDanger)
    fun theActionIsPerformedAndTheResultIsAddedToCapturedInteractions(): ActionUnderTest {
        return ActionUnderTest { givens, interactions ->
            interactions.capture(from(Parrty.A).to(Parrty.B).group("Test").arrowStyle(ArrowStyle.UmlAsynchronousDelete).with("Message", "The Message"))
            givens.get<String>("actionName")?.let {
                interactions.put("result", performer.perform(it))
            }
        }
    }

    private fun theResultStoredInCapturedInteractions(): StateExtractor<String?> {
        return StateExtractor { interactions -> interactions["result"] }
    }

    companion object {
        @JvmStatic
        fun parameterProvider(): Stream<Arguments> {
            return Stream.of(
                    Arguments.arguments("ACTION2", "Performed: ACTION2"),
                    Arguments.arguments("ACTION3", "Performed: ACTION3")
            )
        }
    }

    class ScenarioFoo {
        fun thing(): Int {
            return 666
        }
    }
}