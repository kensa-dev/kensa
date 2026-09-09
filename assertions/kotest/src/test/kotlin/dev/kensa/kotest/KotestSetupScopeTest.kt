package dev.kensa.kotest

import dev.kensa.SetupScope
import dev.kensa.SetupSteps
import dev.kensa.StateCollector
import dev.kensa.context.TestContext
import dev.kensa.context.TestContextHolder
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.SetupStrategy
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class KotestSetupScopeTest {

    private val testContext = TestContext(CapturedInteractions(SetupStrategy.Ignored), Fixtures(), CapturedOutputs())

    @Volatile
    private var settled: String? = null

    @BeforeEach
    fun setUp() = TestContextHolder.bindToCurrentThread(testContext)

    @AfterEach
    fun tearDown() = TestContextHolder.clearFromThread()

    @Test
    fun `the block form of thenEventually polls inside setup and later acts see the settled value`() {
        var seenBySecondAct: String? = null

        val step = object : KotestSetupStep {
            override fun setup(scope: SetupScope) = with(scope) {
                action {
                    Thread {
                        Thread.sleep(50)
                        settled = "settled"
                    }.apply { isDaemon = true }.start()
                }
                thenEventually(2.seconds) {
                    then(StateCollector { settled }) { this shouldBe "settled" }
                    and(StateCollector { settled?.length }) { this shouldBe 7 }
                }
                action { seenBySecondAct = settled }
            }
        }

        testContext.given(SetupSteps(step))

        seenBySecondAct shouldBe "settled"
    }

    @Test
    fun `thenContinually and the single assertion forms are available inside setup too`() {
        val seen = mutableListOf<String>()

        val step = object : KotestSetupStep {
            override fun setup(scope: SetupScope) = with(scope) {
                action { outputs.put("ref", "abc") }
                thenEventually(2.seconds, StateCollector { it.outputs.get<String>("ref") }) { this shouldBe "abc" }
                thenContinually(0.2.seconds, StateCollector { it.outputs.get<String>("ref") }) { this shouldBe "abc" }
                action { seen += it.outputs.get<String>("ref") }
            }
        }

        testContext.given(SetupSteps(step))

        seen shouldBe listOf("abc")
    }
}
