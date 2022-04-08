package dev.kensa.context

import dev.kensa.StateExtractor
import org.awaitility.kotlin.await
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.time.toJavaDuration

object AssertJThen {
    @JvmStatic
    fun <A, T> then(context: TestContext, extractor: StateExtractor<T>, assertProvider: (T?) -> A): A =
        assertProvider(extractor.execute(context.interactions))

    @JvmStatic
    fun <A, T> thenEventually(duration: kotlin.time.Duration, context: TestContext, extractor: StateExtractor<T>, assertProvider: (T?) -> A): A {
        await.atMost(duration.toJavaDuration()).untilAsserted { assertProvider(extractor.execute(context.interactions)) }
        return assertProvider(extractor.execute(context.interactions))
    }

    @JvmStatic
    fun <A, T> thenEventually(timeout: Long, timeUnit: ChronoUnit, context: TestContext, extractor: StateExtractor<T>, assertProvider: (T?) -> A): A {
        await.atMost(Duration.of(timeout, timeUnit)).untilAsserted { assertProvider(extractor.execute(context.interactions)) }
        return assertProvider(extractor.execute(context.interactions))
    }
}