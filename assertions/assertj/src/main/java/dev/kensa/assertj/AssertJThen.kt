package dev.kensa.assertj

import dev.kensa.StateCollector
import dev.kensa.context.TestContext
import org.awaitility.kotlin.await
import java.time.Duration
import java.time.temporal.ChronoUnit

object AssertJThen {
    @JvmStatic
    fun <A, T> then(context: TestContext, collector: StateCollector<T>, assertProvider: (T?) -> A): A =
        assertProvider(collector.execute(context.collectorContext))

    @JvmStatic
    fun <A, T> thenEventually(duration: Duration, context: TestContext, collector: StateCollector<T>, assertProvider: (T?) -> A): A {
        await.atMost(duration).untilAsserted { assertProvider(collector.execute(context.collectorContext)) }
        return assertProvider(collector.execute(context.collectorContext))
    }

    @JvmStatic
    fun <A, T> thenEventually(timeout: Long, timeUnit: ChronoUnit, context: TestContext, collector: StateCollector<T>, assertProvider: (T?) -> A): A {
        await.atMost(Duration.of(timeout, timeUnit)).untilAsserted { assertProvider(collector.execute(context.collectorContext)) }
        return assertProvider(collector.execute(context.collectorContext))
    }
}