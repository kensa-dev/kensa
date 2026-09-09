package dev.kensa.hamkrest

import com.natpryce.hamkrest.equalTo
import dev.kensa.SetupSteps
import dev.kensa.StateCollector
import dev.kensa.context.TestContext
import dev.kensa.context.TestContextHolder
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.SetupStrategy
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HamkrestSetupStepBuilderTest {

    private val testContext = TestContext(CapturedInteractions(SetupStrategy.Ignored), Fixtures(), CapturedOutputs())

    private var settled: String? = null

    @BeforeEach
    fun setUp() = TestContextHolder.bindToCurrentThread(testContext)

    @AfterEach
    fun tearDown() = TestContextHolder.clearFromThread()

    @Test
    fun `hamkrestSetupStep mixes in the hamkrest helpers`() {
        val step = hamkrestSetupStep {
            action { settled = "settled" }
            then(StateCollector { settled }, equalTo("settled"))
        }

        testContext.given(SetupSteps(step))
    }

    @Test
    fun `hamkrestSetupStep fails when the in-block matcher cannot hold`() {
        val step = hamkrestSetupStep {
            action { settled = "settled" }
            then(StateCollector { settled }, equalTo("not-settled"))
        }

        shouldThrow<AssertionError> { testContext.given(SetupSteps(step)) }
    }
}
