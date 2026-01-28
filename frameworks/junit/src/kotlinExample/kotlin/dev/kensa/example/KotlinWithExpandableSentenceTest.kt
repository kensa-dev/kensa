package dev.kensa.example

import com.natpryce.hamkrest.equalTo
import dev.kensa.Action
import dev.kensa.ActionContext
import dev.kensa.GivensContext
import dev.kensa.ExpandableSentence
import dev.kensa.RenderedValue
import dev.kensa.StateCollector
import dev.kensa.fixture.FixtureRegistry.registerFixtures
import dev.kensa.fixture.KotlinTestFixtures
import dev.kensa.fixture.KotlinTestFixtures.ChildStringFixture
import dev.kensa.fixture.KotlinTestFixtures.StringFixture
import dev.kensa.fixture.MyScenario
import dev.kensa.hamkrest.WithHamkrest
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class KotlinWithExpandableSentenceTest : KotlinExampleTest(), WithHamkrest, InterfaceWithExpandableSentence {

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
    fun simpleExpandable() {
        given(somePrerequisites())

        whenever(someActionNoParameters())
        whenever(someActionWith(parameter1 = "my parameter"))

        then(theExtractedValue(), equalTo(aValue))
    }

    @Test
    fun expandableWithMultiLineParameters() {
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
    fun expandableWithScenarioParameters() {
        given(somePrerequisites())

        whenever(someAction(myScenario))
        whenever(someAction(myScenario2))

        then(theExtractedValue(), equalTo(aValue))
    }

    @Test
    fun expandableWithFixtureAndScenarioParameters() {
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
    fun expandableWithExpandableExpression() {
        givenSomePrerequisites()

        wheneverSomeActionWith(aScenarioOf = myScenario)

        then(theExtractedValue(), equalTo(aValue))
    }

    @Test
    fun expandableWithExpandableExpressionInInterface() {
        givenSomePrerequisites()

        whenever { someActionWith4(parameter1 = myScenario.stringValue, parameter2 = "aParameter", parameter3 = "anotherParameter") }

        then(theExtractedValue(), equalTo(aValue))
    }

    @ParameterizedTest
    @ValueSource(strings = ["one", "two"])
    fun parameterisedWithExpandableSentence(value: String) {
        givenSomePrerequisites()
    }

    @ExpandableSentence
    private fun givenSomePrerequisites() {
        given(somePrerequisites())
    }

    @ExpandableSentence
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

    @ExpandableSentence
    private fun someActionWith(@RenderedValue parameter1: String): Action<ActionContext> {
        return someOtherAction(parameter1)
    }

    @ExpandableSentence
    private fun someActionWith2(@RenderedValue parameter1: String, @RenderedValue parameter2: String): Action<ActionContext> {
        someActionWith(parameter2)
        return someOtherAction(parameter1)
    }

    @ExpandableSentence
    private fun someActionWith3(@RenderedValue parameter1: String, @RenderedValue parameter2: String, @RenderedValue parameter3: String): Action<ActionContext> {
        someActionWith(parameter2)
        someActionWith(parameter3)
        return someOtherAction(parameter1)
    }

    @ExpandableSentence
    private fun someAction(@RenderedValue aScenarioOf: MyScenario): Action<ActionContext> {
        return someOtherAction(aScenarioOf.stringValue)
    }

    @ExpandableSentence
    private fun someActionNoParameters(): Action<ActionContext> {
        return someOtherAction("anAction")
    }

    private fun someOtherAction(withAParam: String): Action<ActionContext> {
        return Action { }
    }
}

interface InterfaceWithExpandableSentence {
    @ExpandableSentence
    fun someActionWith4(@RenderedValue parameter1: String, @RenderedValue parameter2: String, @RenderedValue parameter3: String): Action<ActionContext> {
        return someActionWith(parameter1)
    }

    private fun someActionWith(@RenderedValue parameter1: String): Action<ActionContext> = Action { }
}