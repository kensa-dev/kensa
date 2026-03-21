package dev.kensa.example

import com.natpryce.hamkrest.equalTo
import dev.kensa.Action
import dev.kensa.ActionContext
import dev.kensa.GivensContext
import dev.kensa.RenderedValue
import dev.kensa.StateCollector
import dev.kensa.hamkrest.WithHamkrest
import org.junit.jupiter.api.Test

class KotlinWithOutputDisabledTest : KotlinExampleTest(), WithHamkrest {
    @RenderedValue
    private val aValue = "aStringValue"

    @Test
    fun passingTest() {
        given(somePrerequisites())

        whenever(someAction())

        then(theExtractedValue(), equalTo(aValue))
    }

    private fun theExtractedValue(): StateCollector<String?> = StateCollector { aValue }

    private fun somePrerequisites(): Action<GivensContext> = Action { }

    private fun someAction(): Action<ActionContext> = Action { }
}
