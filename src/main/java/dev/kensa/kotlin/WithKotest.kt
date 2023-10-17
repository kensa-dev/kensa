package dev.kensa.kotlin

import dev.kensa.StateExtractor
import dev.kensa.context.KotestThen
import dev.kensa.context.TestContextHolder.testContext
import io.kotest.matchers.Matcher
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface WithKotest {
    fun <T> then(extractor: StateExtractor<T>, block: T.() -> Unit) {
        KotestThen.then(testContext(), extractor, block)
    }

    fun <T> then(extractor: StateExtractor<T>, match: Matcher<T>) {
        KotestThen.then(testContext(), extractor, match)
    }

    fun <T> and(extractor: StateExtractor<T>, block: T.() -> Unit) {
        then(extractor, block)
    }

    fun <T> and(extractor: StateExtractor<T>, match: Matcher<T>) {
        then(extractor, match)
    }

    fun <T> thenEventually(extractor: StateExtractor<T>, match: Matcher<T>) = thenEventually(10.seconds, extractor, match)

    fun <T> thenEventually(duration: Duration, extractor: StateExtractor<T>, match: Matcher<T>) {
        runBlocking {
            KotestThen.thenEventually(duration, testContext(), extractor, match)
        }
    }

    fun <T> thenEventually(extractor: StateExtractor<T>, block: T.() -> Unit = {}) = thenEventually(10.seconds, extractor, block)

    fun <T> thenEventually(duration: Duration, extractor: StateExtractor<T>, block: T.() -> Unit = {}) {
        runBlocking {
            KotestThen.thenEventually(duration, testContext(), extractor, block)
        }
    }
}