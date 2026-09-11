package dev.kensa.example

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import dev.kensa.*
import dev.kensa.hamkrest.WithHamkrest
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class KotlinWithPollingBlocksTest : KotlinExampleTest(), WithHamkrest {

    @Test
    fun passesWithThreeStepsInAZeroArgEventuallyBlock() {
        given(somePrerequisites())

        whenever(someAction())

        thenEventually {
            then(theFirstValue(), equalTo("first"))
            and(theSecondValue(), equalTo("second"))
            and(theThirdValue(), equalTo("third"))
        }
    }

    @Test
    fun passesWithAnAndEventuallyBlock() {
        given(somePrerequisites())

        whenever(someAction())

        then(theFirstValue(), equalTo("first"))

        andEventually(2.seconds) {
            then(theSecondValue(), equalTo("second"))
            and(theThirdValue(), equalTo("third"))
        }
    }

    @Test
    fun passesWithAContinuallyBlockStartingWithAnAndStep() {
        given(somePrerequisites())

        whenever(someAction())

        thenContinually(250.milliseconds) {
            and(theFirstValue(), equalTo("first"))
            and(theSecondValue(), equalTo("second"))
        }
    }

    @Test
    fun passesWithALocalValueBeforeTheFirstStep() {
        given(somePrerequisites())

        whenever(someAction())

        thenEventually(2.seconds) {
            val theOrder = theFirstValue()
            then(theOrder, equalTo("first"))
            and(theSecondValue(), equalTo("second"))
        }
    }

    @Test
    fun passesWithASingleCollectorBlock() {
        given(somePrerequisites())

        whenever(someAction())

        thenEventually(theFirstValue()) {
            assertThat(this, equalTo("first"))
        }
    }

    private fun theFirstValue() = StateCollector { "first" }

    private fun theSecondValue() = StateCollector { "second" }

    private fun theThirdValue() = StateCollector { "third" }

    private fun somePrerequisites() = Action<GivensContext> {}

    private fun someAction() = Action<ActionContext> {}
}
