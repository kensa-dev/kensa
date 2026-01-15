package dev.kensa.example

import com.natpryce.hamkrest.equalTo
import dev.kensa.ActionUnderTest
import dev.kensa.GivensBuilder
import dev.kensa.RenderedValue
import dev.kensa.StateExtractor
import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.state.Givens
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

    private fun theExtractedValue(): StateExtractor<String?> = StateExtractor { _ -> aValue }

    private fun somePrerequisites(): GivensBuilder = GivensBuilder { givens: Givens -> givens.put("foo", "bar") }

    private fun someAction(): ActionUnderTest = ActionUnderTest { _, _ -> }
}
