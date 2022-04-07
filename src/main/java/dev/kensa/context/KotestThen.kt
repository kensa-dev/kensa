package dev.kensa.context

import dev.kensa.StateExtractor
import io.kotest.matchers.Matcher
import io.kotest.matchers.invokeMatcher

object KotestThen {
    fun <T> then(testContext: TestContext, extractor: StateExtractor<T>, block: T.() -> Unit) {
        block(extractor.execute(testContext.interactions))
    }

    fun <T> then(testContext: TestContext, extractor: StateExtractor<T>, match: Matcher<T>) {
        invokeMatcher(extractor.execute(testContext.interactions), match)
    }
}