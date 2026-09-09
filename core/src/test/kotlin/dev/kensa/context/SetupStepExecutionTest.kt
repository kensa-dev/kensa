package dev.kensa.context

import dev.kensa.Action
import dev.kensa.ActionBlockBuilder.Companion.buildActions
import dev.kensa.GivensBlockBuilder.Companion.buildGivens
import dev.kensa.SetupScope
import dev.kensa.SetupStep
import dev.kensa.SetupSteps
import dev.kensa.VerificationBlockBuilder.Companion.verify
import dev.kensa.and
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.SetupStrategy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class SetupStepExecutionTest {

    @Test
    fun `a single step runs givens then actions then verify in order`() {
        val calls = mutableListOf<String>()
        val context = TestContext(CapturedInteractions(SetupStrategy.Grouped), Fixtures(), CapturedOutputs())
        val step = RecordingSetupStep("step", calls)

        context.given(SetupSteps(step))

        calls shouldContainExactly listOf("step.givens", "step.actions", "step.verify")
    }

    @Test
    fun `two steps chained with and both run their triple in order`() {
        val calls = mutableListOf<String>()
        val context = TestContext(CapturedInteractions(SetupStrategy.Grouped), Fixtures(), CapturedOutputs())
        val first = RecordingSetupStep("first", calls)
        val second = RecordingSetupStep("second", calls)

        context.given(first.and(second))

        calls shouldContainExactly listOf(
            "first.givens", "first.actions", "first.verify",
            "second.givens", "second.actions", "second.verify"
        )
    }

    @Test
    fun `steps built as a list run in order`() {
        val calls = mutableListOf<String>()
        val context = TestContext(CapturedInteractions(SetupStrategy.Grouped), Fixtures(), CapturedOutputs())
        val first = RecordingSetupStep("first", calls)
        val second = RecordingSetupStep("second", calls)
        val steps: List<SetupStep> = listOf(first, second)

        context.given(SetupSteps(steps))

        calls shouldContainExactly listOf(
            "first.givens", "first.actions", "first.verify",
            "second.givens", "second.actions", "second.verify"
        )
    }

    @Test
    fun `a step overriding setup runs its own scope calls instead of the triple`() {
        val calls = mutableListOf<String>()
        var seen: String? = null
        val context = TestContext(CapturedInteractions(SetupStrategy.Grouped), Fixtures(), CapturedOutputs())
        val step = SetupOverridingStep(calls) { seen = it }

        context.given(SetupSteps(step))

        seen shouldBe "abc"
        calls shouldContainExactly emptyList()
    }

    @Test
    fun `given and collect share the test's outputs`() {
        var collected: Int? = null
        val context = TestContext(CapturedInteractions(SetupStrategy.Grouped), Fixtures(), CapturedOutputs())
        val step = GivenCollectSetupStep { collected = it }

        context.given(SetupSteps(step))

        collected shouldBe 1
    }

    @Test
    fun `fixtures and outputs on the scope are the test's own instances`() {
        var seenFixtures: Fixtures? = null
        var seenOutputs: CapturedOutputs? = null
        val context = TestContext(CapturedInteractions(SetupStrategy.Grouped), Fixtures(), CapturedOutputs())
        val step = ScopeIdentitySetupStep { fixtures, outputs ->
            seenFixtures = fixtures
            seenOutputs = outputs
        }

        context.given(SetupSteps(step))

        seenFixtures shouldBeSameInstanceAs context.fixtures
        seenOutputs shouldBeSameInstanceAs context.outputs
    }

    @Test
    fun `interactions inside setup are not marked as under test`() {
        var seenDuringSetup: Boolean? = null
        val context = TestContext(CapturedInteractions(SetupStrategy.Grouped), Fixtures(), CapturedOutputs())
        val step = InteractionsFlagSetupStep { seenDuringSetup = it }

        context.given(SetupSteps(step))

        seenDuringSetup shouldBe false
        context.interactions.isUnderTest shouldBe false

        context.whenever { }

        context.interactions.isUnderTest shouldBe true
    }

    @Test
    fun `verifyEventually retries the check until it passes`() {
        val counter = AtomicInteger(0)
        val context = TestContext(CapturedInteractions(SetupStrategy.Grouped), Fixtures(), CapturedOutputs())
        val step = AwaitSetupStep { scope ->
            scope.verifyEventually(duration = 2.seconds, interval = 10.milliseconds) {
                if (counter.incrementAndGet() < 3) throw AssertionError("not yet")
            }
        }

        context.given(SetupSteps(step))

        counter.get() shouldBe 3
    }

    @Test
    fun `verifyEventually times out and rethrows the last assertion error`() {
        val context = TestContext(CapturedInteractions(SetupStrategy.Grouped), Fixtures(), CapturedOutputs())
        val step = AwaitSetupStep { scope ->
            scope.verifyEventually(duration = 100.milliseconds, interval = 10.milliseconds) {
                throw AssertionError("still wrong")
            }
        }
        val mark = TimeSource.Monotonic.markNow()

        val thrown = shouldThrow<AssertionError> {
            context.given(SetupSteps(step))
        }

        thrown.message shouldBe "still wrong"
        mark.elapsedNow() shouldBeGreaterThanOrEqualTo 100.milliseconds
    }

    @Test
    fun `verifyEventually retries a non-assertion exception and rethrows it on timeout`() {
        val counter = AtomicInteger(0)
        val context = TestContext(CapturedInteractions(SetupStrategy.Grouped), Fixtures(), CapturedOutputs())
        val step = AwaitSetupStep { scope ->
            scope.verifyEventually(duration = 100.milliseconds, interval = 10.milliseconds) {
                counter.incrementAndGet()
                throw IllegalStateException("not ready")
            }
        }

        shouldThrow<IllegalStateException> {
            context.given(SetupSteps(step))
        }

        counter.get() shouldBeGreaterThanOrEqualTo 2
    }

    @Test
    fun `verifyEventually with defaults compiles and returns immediately`() {
        val context = TestContext(CapturedInteractions(SetupStrategy.Grouped), Fixtures(), CapturedOutputs())
        val step = AwaitSetupStep { scope -> scope.verifyEventually { } }

        context.given(SetupSteps(step))
    }

    @Test
    fun `verifyEventually is callable with java time Duration and an Action`() {
        val counter = AtomicInteger(0)
        val context = TestContext(CapturedInteractions(SetupStrategy.Grouped), Fixtures(), CapturedOutputs())
        val step = AwaitSetupStep { scope ->
            scope.verifyEventually(java.time.Duration.ofSeconds(2), java.time.Duration.ofMillis(10), Action {
                if (counter.incrementAndGet() < 3) throw AssertionError("not yet")
            })
        }

        context.given(SetupSteps(step))

        counter.get() shouldBe 3
    }

    @Test
    fun `a step overriding verify propagates the exception unchanged`() {
        val context = TestContext(CapturedInteractions(SetupStrategy.Grouped), Fixtures(), CapturedOutputs())
        val step = VerifyThrowingSetupStep()

        val thrown = shouldThrow<IllegalStateException> {
            context.given(SetupSteps(step))
        }

        thrown.message shouldBe "x"
    }

    @Test
    fun `scope verify runs the block once against the collector context`() {
        val calls = mutableListOf<String>()
        val context = TestContext(CapturedInteractions(SetupStrategy.Grouped), Fixtures(), CapturedOutputs())
        val step = AwaitSetupStep { scope ->
            scope.verify { calls.add("verify") }
        }

        context.given(SetupSteps(step))

        calls shouldContainExactly listOf("verify")
    }

    @Test
    fun `scope verify propagates an assertion failure`() {
        val context = TestContext(CapturedInteractions(SetupStrategy.Grouped), Fixtures(), CapturedOutputs())
        val step = AwaitSetupStep { scope ->
            scope.verify { throw AssertionError("nope") }
        }

        val thrown = shouldThrow<AssertionError> {
            context.given(SetupSteps(step))
        }

        thrown.message shouldBe "nope"
    }
}

private class RecordingSetupStep(private val name: String, private val calls: MutableList<String>) : SetupStep {

    override fun givens() = buildGivens { calls.add("$name.givens") }

    override fun actions() = buildActions { calls.add("$name.actions") }

    override fun verify() = verify { calls.add("$name.verify") }
}

private class SetupOverridingStep(private val calls: MutableList<String>, private val onSeen: (String?) -> Unit) : SetupStep {

    override fun setup(scope: SetupScope) {
        scope.action { it.outputs.put("ref", "abc") }
        scope.action { ctx -> onSeen(ctx.outputs["ref"]) }
    }

    override fun givens() = buildGivens { calls.add("givens") }

    override fun actions() = buildActions { calls.add("actions") }

    override fun verify() = verify { calls.add("verify") }
}

private class GivenCollectSetupStep(private val onCollected: (Int) -> Unit) : SetupStep {

    override fun setup(scope: SetupScope) {
        scope.given { it.outputs.put("g", 1) }
        onCollected(scope.collect { it.outputs.get<Int>("g") })
    }

    override fun givens() = buildGivens { }

    override fun actions() = buildActions { }

    override fun verify() = verify { }
}

private class ScopeIdentitySetupStep(private val onSeen: (Fixtures, CapturedOutputs) -> Unit) : SetupStep {

    override fun setup(scope: SetupScope) {
        onSeen(scope.fixtures, scope.outputs)
    }

    override fun givens() = buildGivens { }

    override fun actions() = buildActions { }

    override fun verify() = verify { }
}

private class InteractionsFlagSetupStep(private val onSeen: (Boolean) -> Unit) : SetupStep {

    override fun setup(scope: SetupScope) {
        scope.action { onSeen(it.interactions.isUnderTest) }
    }

    override fun givens() = buildGivens { }

    override fun actions() = buildActions { }

    override fun verify() = verify { }
}

private class AwaitSetupStep(private val onSetup: (SetupScope) -> Unit) : SetupStep {

    override fun setup(scope: SetupScope) {
        onSetup(scope)
    }

    override fun givens() = buildGivens { }

    override fun actions() = buildActions { }

    override fun verify() = verify { }
}

private class VerifyThrowingSetupStep : SetupStep {

    override fun givens() = buildGivens { }

    override fun actions() = buildActions { }

    override fun verify() = verify { throw IllegalStateException("x") }
}
