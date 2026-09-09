package dev.kensa

import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinDuration

interface SetupScope {
    val fixtures: Fixtures
    val outputs: CapturedOutputs
    fun given(action: Action<GivensContext>)
    fun action(action: Action<ActionContext>)
    fun <T> collect(collector: StateCollector<T>): T
    fun verify(block: (CollectorContext) -> Unit)
    fun verifyEventually(duration: Duration = 10.seconds, interval: Duration = 25.milliseconds, check: (CollectorContext) -> Unit)
    fun verifyEventually(duration: java.time.Duration, interval: java.time.Duration, check: Action<CollectorContext>) =
        verifyEventually(duration.toKotlinDuration(), interval.toKotlinDuration()) { check.execute(it) }
}
