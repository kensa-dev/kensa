package dev.kensa

import com.natpryce.hamkrest.equalTo
import dev.kensa.fixture.FixtureRegistry.registerFixtures
import dev.kensa.fixture.KotlinTestFixtures
import dev.kensa.fixture.KotlinTestFixtures.ChildStringFixture
import dev.kensa.fixture.KotlinTestFixtures.StringFixture
import dev.kensa.fixture.MyScenario
import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.junit.KensaTest
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens
import org.junit.jupiter.api.Test

class KotlinWithNestedSentenceTest : KensaTest, WithHamkrest {

    init {
        registerFixtures(KotlinTestFixtures)
    }

    @RenderedValue
    private val aValue = "aStringValue"

    @RenderedValue
    private val myScenario = MyScenario(aValue)

    @RenderedValue
    private val myScenario2 = MyScenario("Meh")

    private fun myString(): String = "myStringThing"

    @Test
    fun simpleNested() {
        given(somePrerequisites())

        whenever(someActionNoParameters())
        whenever(someActionWith(parameter1 = "my parameter"))

        then(theExtractedValue(), equalTo(aValue))
    }

    @Test
    fun nestedWithMultiLineParameters() {
        whenever(someActionWith2(
            parameter1 = "my parameter",
            parameter2 = "my parameter2"
        ))

        myLambdaBlock { someActionWith2(parameter1 = myString(), parameter2 = myString()) }
        myLambdaBlock {
            someActionWith2(
                parameter1 = myString(),
                parameter2 = myString()
            )
        }

        then(theExtractedValue(), equalTo(aValue))
    }

    @Test
    fun nestedWithScenarioParameters() {
        given(somePrerequisites())

        whenever(someAction(myScenario))
        whenever(someAction(myScenario2))

        then(theExtractedValue(), equalTo(aValue))
    }

    @Test
    fun nestedWithFixtureAndScenarioParameters() {
        given(somePrerequisites())

        whenever(someActionWith3(
            parameter1 = myScenario.stringValue,
            parameter2 = fixtures(StringFixture),
            parameter3 = fixtures(ChildStringFixture)
        ))

        then(theExtractedValue(), equalTo(aValue))
    }

    private fun myLambdaBlock(block: () -> Unit) = block()

    private fun theExtractedValue(): StateExtractor<String?> {
        return StateExtractor { interactions: CapturedInteractions? -> aValue }
    }

    private fun somePrerequisites(): GivensBuilder {
        return GivensBuilder { givens: Givens -> givens.put("foo", "bar") }
    }

    @NestedSentence
    private fun someActionWith(@RenderedValue parameter1: String): ActionUnderTest {
        return someActionUnderTest(parameter1)
    }

    @NestedSentence
    private fun someActionWith2(@RenderedValue parameter1: String, @RenderedValue parameter2: String): ActionUnderTest {
        someActionWith(parameter2)
        return someActionUnderTest(parameter1)
    }

    @NestedSentence
    private fun someActionWith3(@RenderedValue parameter1: String, @RenderedValue parameter2: String, @RenderedValue parameter3: String): ActionUnderTest {
        someActionWith(parameter2)
        someActionWith(parameter3)
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
        return ActionUnderTest { _, _ -> }
    }

}