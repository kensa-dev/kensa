package dev.kensa

import com.natpryce.hamkrest.equalTo
import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.junit.KensaTest
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens
import org.junit.jupiter.api.Test

class KotlinWithSinglePassingTest : KensaTest, WithHamkrest {
    @SentenceValue
    private val aValue = "aStringValue"

    @Test
    fun passingTest() {
        given(somePrerequisites())

        whenever(someAction())

        then(theExtractedValue(), equalTo(aValue))
    }

    private fun theExtractedValue(): StateExtractor<String?> = StateExtractor { interactions: CapturedInteractions -> aValue }

    private fun somePrerequisites(): GivensBuilder = GivensBuilder { givens: Givens, _ -> givens.put("foo", "bar") }

    private fun someAction(): ActionUnderTest = ActionUnderTest { _, _, _->}
}
