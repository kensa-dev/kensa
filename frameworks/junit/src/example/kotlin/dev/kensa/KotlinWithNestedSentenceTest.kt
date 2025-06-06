package dev.kensa

import com.natpryce.hamkrest.equalTo
import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.junit.KensaTest
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens
import org.junit.jupiter.api.Test

class KotlinWithNestedSentenceTest : KensaTest, WithHamkrest {

    @RenderedValue
    private val aValue = "aStringValue"

    @Test
    fun passingTest() {
        given(somePrerequisites())

        whenever(someAction())

        then(theExtractedValue(), equalTo(aValue))
    }

    private fun theExtractedValue(): StateExtractor<String?> {
        return StateExtractor { interactions: CapturedInteractions? -> aValue }
    }

    private fun somePrerequisites(): GivensBuilderWithFixtures {
        return GivensBuilderWithFixtures { givens: Givens, _ -> givens.put("foo", "bar") }
    }

    @NestedSentence
    private fun someAction(): ActionUnderTest {
        return someActionUnderTest()
    }

    private fun someActionUnderTest(): ActionUnderTest {
        return ActionUnderTest { _, _ ->}
    }

}