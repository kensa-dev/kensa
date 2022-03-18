package dev.kensa.context

import dev.kensa.StateExtractor
import org.awaitility.kotlin.await

object AssertJThen {
    @JvmStatic
    fun <A, T> then(context: TestContext, extractor: StateExtractor<T>, assertProvider: (T?) -> A): A =
        assertProvider(extractor.execute(context.interactions))

    @JvmStatic
    fun <A, T> thenEventually(context: TestContext, extractor: StateExtractor<T>, assertProvider: (T?) -> A): A {
        await.untilAsserted { assertProvider(extractor.execute(context.interactions)) }
        return assertProvider(extractor.execute(context.interactions))
    }
}