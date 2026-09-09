package dev.kensa.context

import dev.kensa.Action
import dev.kensa.ActionContext
import dev.kensa.CollectorContext
import dev.kensa.GivensContext
import dev.kensa.SetupScope
import dev.kensa.StateCollector
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

internal class TestContextSetupScope(private val testContext: TestContext) : SetupScope {

    override val fixtures: Fixtures = testContext.fixtures
    override val outputs: CapturedOutputs = testContext.outputs

    override fun given(action: Action<GivensContext>) {
        action.execute(testContext.givensContext)
    }

    override fun action(action: Action<ActionContext>) {
        action.execute(testContext.actionContext)
    }

    override fun <T> collect(collector: StateCollector<T>): T = collector.execute(testContext.collectorContext)

    override fun verify(block: (CollectorContext) -> Unit) {
        block(testContext.collectorContext)
    }

    override fun verifyEventually(duration: Duration, interval: Duration, check: (CollectorContext) -> Unit) {
        val mark = TimeSource.Monotonic.markNow()
        while (true) {
            try {
                check(testContext.collectorContext)
                return
            } catch (t: Throwable) {
                if (mark.elapsedNow() >= duration) throw t
                Thread.sleep(minOf(interval, duration - mark.elapsedNow()).inWholeMilliseconds.coerceAtLeast(0))
            }
        }
    }
}
