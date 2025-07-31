package dev.kensa

import com.natpryce.hamkrest.equalTo
import dev.kensa.fixture.FixtureRegistry.registerFixtures
import dev.kensa.fixture.KotlinTestFixtures
import dev.kensa.fixture.KotlinTestFixtures.ChildStringFixture
import dev.kensa.fixture.KotlinTestFixtures.StringFixture
import dev.kensa.fixture.MyScenario
import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.junit.KensaTest
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

    private fun myString(foo: String = ""): String = "myStringThing"

    @Test
    fun simpleNested() {
        given(somePrerequisites())

        whenever(someActionNoParameters())
        whenever(someActionWith(parameter1 = "my parameter"))

        then(theExtractedValue(), equalTo(aValue))
    }

    @Test
    fun nestedWithMultiLineParameters() {
        whenever(
            someActionWith2(
                parameter1 = "my parameter",
                parameter2 = "my parameter2"
            )
        )

        myLambdaBlock { someActionWith2(parameter1 = myString(), parameter2 = myString("Foo")) }
        myLambdaBlock {
            someActionWith2(
                parameter1 = myString("1"),
                parameter2 = myString("2")
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

        whenever(
            someActionWith3(
                parameter1 = myScenario.stringValue,
                parameter2 = fixtures(StringFixture),
                parameter3 = fixtures(ChildStringFixture)
            )
        )

        then(theExtractedValue(), equalTo(aValue))
    }

    @Test
    fun nestedWithNestedExpression() {
        givenSomePrerequisites()

        wheneverSomeActionWith(aScenarioOf = myScenario)

        then(theExtractedValue(), equalTo(aValue))
    }

    @NestedSentence
    private fun givenSomePrerequisites() {
        given(somePrerequisites())
    }

    @NestedSentence
    private fun wheneverSomeActionWith(@RenderedValue aScenarioOf: MyScenario) {
        whenever(someAction(aScenarioOf))
    }

    private fun myLambdaBlock(block: () -> Unit) = block()

    private fun theExtractedValue(): StateCollector<String?> {
        return StateCollector { aValue }
    }

    private fun somePrerequisites(): Action<GivensContext> {
        return Action { }
    }

    @NestedSentence
    private fun someActionWith(@RenderedValue parameter1: String): Action<ActionContext> {
        return someAction(parameter1)
    }

    @NestedSentence
    private fun someActionWith2(@RenderedValue parameter1: String, @RenderedValue parameter2: String): Action<ActionContext> {
        someActionWith(parameter2)
        return someAction(parameter1)
    }

    @NestedSentence
    private fun someActionWith3(@RenderedValue parameter1: String, @RenderedValue parameter2: String, @RenderedValue parameter3: String): Action<ActionContext> {
        someActionWith(parameter2)
        someActionWith(parameter3)
        return someAction(parameter1)
    }

    @NestedSentence
    private fun someAction(@RenderedValue aScenarioOf: MyScenario): Action<ActionContext> {
        return someAction(aScenarioOf.stringValue)
    }

    @NestedSentence
    private fun someActionNoParameters(): Action<ActionContext> {
        return someAction("anAction")
    }

    private fun someAction(withAParam: String): Action<ActionContext> {
        return Action { }
    }
}