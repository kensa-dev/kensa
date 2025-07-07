package dev.kensa

import com.natpryce.hamkrest.equalTo
import dev.kensa.fixture.MyScenario
import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.junit.KensaTest
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens
import org.junit.jupiter.api.Test

class KotlinWithNestedSentenceTest : KensaTest, WithHamkrest {

    @RenderedValue
    private val aValue = "aStringValue"

    @RenderedValue
    private val myScenario = MyScenario(aValue)

    @RenderedValue
    private val myScenario2 = MyScenario("Meh")

    @Test
    fun passingTest() {
        given(somePrerequisites())

        whenever(someActionNoParameters())
        whenever(someActionWith(parameter1 = "my parameter"))

        then(theExtractedValue(), equalTo(aValue))
    }

    @Test
    fun testWithNestedScenarioParameter() {
        given(somePrerequisites())

        whenever(someAction(myScenario))

        whenever(someAction(myScenario2))
    }

    private fun theExtractedValue(): StateExtractor<String?> {
        return StateExtractor { interactions: CapturedInteractions? -> aValue }
    }

    private fun somePrerequisites(): GivensBuilderWithFixtures {
        return GivensBuilderWithFixtures { givens: Givens, _ -> givens.put("foo", "bar") }
    }

    @NestedSentence
    private fun someActionWith(@RenderedValue parameter1: String): ActionUnderTest {
        return someActionUnderTest(parameter1)
    }

    @NestedSentence
    private fun someAction(@RenderedValue aScenarioOf: MyScenario): ActionUnderTest {
        return someActionUnderTest(aScenarioOf.stringValue)
    }

    @NestedSentence
    private fun someActionNoParameters(): ActionUnderTest {
        return someActionUnderTest("anAction")
    }

    private fun someActionUnderTest(withAParam: String): ActionUnderTest {
        return ActionUnderTest { _, _ ->}
    }

}