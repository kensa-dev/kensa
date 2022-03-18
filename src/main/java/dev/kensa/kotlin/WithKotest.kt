package dev.kensa.kotlin

import dev.kensa.StateExtractor
import dev.kensa.context.KotestThen
import dev.kensa.context.TestContextHolder.testContext
import io.kotest.matchers.Matcher

interface WithKotest {
    fun <T> then(extractor: StateExtractor<T>, block: T.() -> Unit) {
        KotestThen.then(testContext(), extractor, block)
    }

    fun <T> then(extractor: StateExtractor<T>, match: Matcher<T>) {
        KotestThen.then(testContext(), extractor, match)
    }
}