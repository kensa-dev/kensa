package dev.kensa.example

import com.natpryce.hamkrest.equalTo
import dev.kensa.*
import dev.kensa.hamkrest.WithHamkrest
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class KotlinWithParallelAssertionsTest : KotlinExampleTest(), WithHamkrest {

    @Test
    fun passesWithMultipleEventualAssertions() {
        given(somePrerequisites())

        whenever(someAction())

        thenEventually(2.seconds) {
            then(theFirstValue(), equalTo("first"))
            and(theSecondValue(), equalTo("second"))
        }
    }

    @Test
    fun passesWithMultipleContinualAssertions() {
        given(somePrerequisites())

        whenever(someAction())

        thenContinually(250.milliseconds) {
            then(theFirstValue(), equalTo("first"))
            and(theSecondValue(), equalTo("second"))
        }
    }

    private fun theFirstValue() = StateCollector { "first" }

    private fun theSecondValue() = StateCollector { "second" }

    private fun somePrerequisites() = Action<GivensContext> {}

    private fun someAction() = Action<ActionContext> {}
}
