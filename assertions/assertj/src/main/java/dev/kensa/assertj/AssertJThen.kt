package dev.kensa.assertj

import dev.kensa.StateExtractor
import dev.kensa.context.TestContext
import org.awaitility.kotlin.await
import java.time.Duration
import java.time.temporal.ChronoUnit

object AssertJThen {
    @JvmStatic
    fun <A, T> then(context: TestContext, extractor: StateExtractor<T>, assertProvider: (T?) -> A): A =
        assertProvider(extractor.execute(context.interactions))

    @JvmStatic
    fun <A, T> thenEventually(duration: Duration, context: TestContext, extractor: StateExtractor<T>, assertProvider: (T?) -> A): A {
        await.atMost(duration).untilAsserted { assertProvider(extractor.execute(context.interactions)) }
        return assertProvider(extractor.execute(context.interactions))
    }

    @JvmStatic
    fun <A, T> thenEventually(timeout: Long, timeUnit: ChronoUnit, context: TestContext, extractor: StateExtractor<T>, assertProvider: (T?) -> A): A {
        await.atMost(Duration.of(timeout, timeUnit)).untilAsserted { assertProvider(extractor.execute(context.interactions)) }
        return assertProvider(extractor.execute(context.interactions))
    }
}