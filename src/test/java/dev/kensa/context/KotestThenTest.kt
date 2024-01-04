package dev.kensa.context

import dev.kensa.StateExtractor
import io.kotest.matchers.be
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class KotestThenTest {

    private val testContext = mock<TestContext>()
    private val stateExtractor = mock<StateExtractor<String>>()

    @Test
    fun `does not invoke extractor more than once`() {
        whenever(stateExtractor.execute(anyOrNull())).thenReturn("result")

        KotestThen.then(testContext, stateExtractor, be("result"))

        verify(stateExtractor, times(1)).execute(anyOrNull())
    }
}