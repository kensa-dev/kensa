package dev.kensa.kotlin

import dev.kensa.StateExtractor
import dev.kensa.context.TestContextHolder
import io.kotest.matchers.Matcher

interface WithKotest {
    fun <T> then(extractor: StateExtractor<T>, block: T.() -> Unit) {
        block(extractor.execute(TestContextHolder.testContext().interactions))
    }

    fun <T> then(extractor: StateExtractor<T>, match: Matcher<T>) {
        match.test(extractor.execute(TestContextHolder.testContext().interactions))
    }
}