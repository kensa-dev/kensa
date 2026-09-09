package dev.kensa.hamcrest

import dev.kensa.SetupSteps
import dev.kensa.StateCollector
import dev.kensa.context.TestContext
import dev.kensa.context.TestContextHolder
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.SetupStrategy
import io.kotest.assertions.throwables.shouldThrow
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HamcrestSetupStepBuilderTest {

    private val testContext = TestContext(CapturedInteractions(SetupStrategy.Ignored), Fixtures(), CapturedOutputs())

    private var settled: String? = null

    @BeforeEach
    fun setUp() = TestContextHolder.bindToCurrentThread(testContext)

    @AfterEach
    fun tearDown() = TestContextHolder.clearFromThread()

    @Test
    fun `hamcrestSetupStep mixes in the hamcrest helpers`() {
        val step = hamcrestSetupStep {
            action { settled = "settled" }
            then(StateCollector { settled }, equalTo("settled"))
        }

        testContext.given(SetupSteps(step))
    }

    @Test
    fun `hamcrestSetupStep fails when the in-block matcher cannot hold`() {
        val step = hamcrestSetupStep {
            action { settled = "settled" }
            then(StateCollector { settled }, equalTo("not-settled"))
        }

        shouldThrow<AssertionError> { testContext.given(SetupSteps(step)) }
    }
}
