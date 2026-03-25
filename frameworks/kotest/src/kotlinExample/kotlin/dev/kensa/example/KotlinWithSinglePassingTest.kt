package dev.kensa.example

import dev.kensa.*
import dev.kensa.kotest.WithKotest
import io.kotest.matchers.be

class KotlinWithSinglePassingTest : KotlinExampleTest(), WithKotest {
    @RenderedValue
    private val aValue = "aStringValue"

    @Test
    fun passingTest() {
        given(somePrerequisites())

        whenever(someAction())

        then(theExtractedValue(), be(aValue))
    }

    private fun theExtractedValue(): StateCollector<String?> = StateCollector { a -> aValue }

    private fun somePrerequisites(): Action<GivensContext> = {  it.outputs.put("foo", "bar") }

    private fun someAction(): Action<ActionContext> = { _ -> }
}
