package dev.kensa.context

import dev.kensa.StateExtractor
import io.kotest.matchers.Matcher

object KotestThen {
    fun <T> then(testContext: TestContext, extractor: StateExtractor<T>, block: T.() -> Unit) {
        block(extractor.execute(testContext.interactions))
    }

    fun <T> then(testContext: TestContext, extractor: StateExtractor<T>, match: Matcher<T>) {
        match.test(extractor.execute(testContext.interactions))
    }
}