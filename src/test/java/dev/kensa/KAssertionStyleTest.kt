package dev.kensa

import dev.kensa.Kensa.konfigure
import dev.kensa.Section.*
import dev.kensa.kotlin.KotlinKensaTest
import dev.kensa.kotlin.WithAssertJ
import dev.kensa.kotlin.WithHamcrest
import dev.kensa.sentence.Acronym
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens
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
        }
    }

    @Test
    @Notes("Method Notes")
    @Issue("ISS-007")
    fun canUseAssertJStyle() {
        given(someActionNameIsAddedToGivens())

        whenever(theActionIsPerformedAndTheResultIsAddedToCapturedInteractions())

        then(theResultStoredInCapturedInteractions()).isEqualTo(theExpectedResult)
        withAllTheNestedThings()
        then(foo()).isEqualTo(scenario.thing())
        then(foo1())
                .isEqualTo("777")
        then(foo())
                .isEqualTo(666)
    }

    @NestedSentence
    private fun withAllTheNestedThings() {
        then(foo()).isEqualTo(scenario.thing())
    }

    private fun foo(): StateExtractor<Int?> {
        return object : StateExtractor<Int?> {
            override fun execute(interactions: CapturedInteractions): Int? {
                return 666
            }
        }
    }

    private fun foo1(): StateExtractor<String?> {
        return object : StateExtractor<String?> {
            override fun execute(interactions: CapturedInteractions): String {
                return "777"
            }
        }
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
        return object : GivensBuilder {
            override fun build(givens: Givens) {
                givens.put("actionName", actionName)
            }
        }
    }

    private fun someActionNameIsAddedToGivens(): GivensBuilder {
        return object : GivensBuilder {
            override fun build(givens: Givens) {
                givens.put("actionName", actionName)
            }
        }
    }

    private fun theActionIsPerformedAndTheResultIsAddedToCapturedInteractions(): ActionUnderTest {
        return object : ActionUnderTest {
            override fun execute(givens: Givens, interactions: CapturedInteractions) {
                givens.get<String>("actionName")?.let {
                    interactions.put("result", performer.perform(it))
                }
            }
        }
    }

    private fun theResultStoredInCapturedInteractions(): StateExtractor<String?> {
        return object : StateExtractor<String?> {
            override fun execute(interactions: CapturedInteractions): String? {
                return interactions["result"]
            }
        }
    }

    companion object {
        @JvmStatic
        private fun parameterProvider(): Stream<Arguments> {
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