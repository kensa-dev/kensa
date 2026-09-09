package dev.kensa.kotest

import dev.kensa.SetupSteps
import dev.kensa.StateCollector
import dev.kensa.context.TestContext
import dev.kensa.context.TestContextHolder
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.SetupStrategy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class KotestSetupStepBuilderTest {

    private val testContext = TestContext(CapturedInteractions(SetupStrategy.Ignored), Fixtures(), CapturedOutputs())

    @Volatile
    private var settled: String? = null

    @BeforeEach
    fun setUp() = TestContextHolder.bindToCurrentThread(testContext)

    @AfterEach
    fun tearDown() = TestContextHolder.clearFromThread()

    @Test
    fun `kotestSetupStep mixes in the kotest helpers`() {
        val step = kotestSetupStep {
            action { settled = "settled" }
            thenEventually(2.seconds, StateCollector { settled }) { this shouldBe "settled" }
        }

        testContext.given(SetupSteps(step))
    }

    @Test
    fun `kotestSetupStep fails when the in-block matcher cannot hold`() {
        val step = kotestSetupStep {
            action { settled = "settled" }
            thenEventually(100.milliseconds, StateCollector { settled }) { this shouldBe "not-settled" }
        }

        shouldThrow<AssertionError> { testContext.given(SetupSteps(step)) }
    }
}
