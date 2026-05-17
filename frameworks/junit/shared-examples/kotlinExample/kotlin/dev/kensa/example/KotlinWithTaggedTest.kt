package dev.kensa.example

import com.natpryce.hamkrest.equalTo
import dev.kensa.*
import dev.kensa.hamkrest.WithHamkrest
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("smoke")
@Tag("regression")
class KotlinWithTaggedTest : KotlinExampleTest(), WithHamkrest {
    @RenderedValue
    private val aValue = "aStringValue"

    @Tag("slow")
    @Test
    fun passingTest() {
        given(somePrerequisites())

        whenever(someAction())

        then(theExtractedValue(), equalTo(aValue))
    }

    private fun theExtractedValue() = StateCollector { aValue }

    private fun somePrerequisites() = Action<GivensContext> {}

    private fun someAction() = Action<ActionContext> {}
}
