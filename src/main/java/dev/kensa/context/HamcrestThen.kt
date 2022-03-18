package dev.kensa.context

import dev.kensa.StateExtractor
import org.awaitility.kotlin.await
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert

object HamcrestThen {
    @JvmStatic
    fun <T> then(context: TestContext, extractor: StateExtractor<T>, matcher: Matcher<in T>) {
        MatcherAssert.assertThat(extractor.execute(context.interactions), matcher)
    }

    @JvmStatic
    fun <T> thenEventually(context: TestContext, extractor: StateExtractor<T>, matcher: Matcher<in T>) {
        await.untilAsserted { MatcherAssert.assertThat(extractor.execute(context.interactions), matcher) }
        MatcherAssert.assertThat(extractor.execute(context.interactions), matcher)
    }
}