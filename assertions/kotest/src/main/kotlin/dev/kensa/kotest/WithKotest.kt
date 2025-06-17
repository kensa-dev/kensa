package dev.kensa.kotest

import dev.kensa.StateExtractor
import dev.kensa.StateExtractorWithFixtures
import dev.kensa.context.TestContextHolder.testContext
import io.kotest.matchers.Matcher
import io.kotest.matchers.invokeMatcher
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

interface WithKotest {

    fun <T> then(spec: ThenSpec<T>) {
        with(spec) {
            KotestThen.then(testContext(), extractor) {
                invokeMatcher(this, matcher)
                testContext().apply { onMatch(this@then) }
            }
        }
    }

    fun <T> and(spec: ThenSpec<T>): Unit = then(spec)

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

    fun <T> thenEventually(spec: ThenSpec<T>): Unit = thenEventually(10.seconds, spec)
    fun <T> andEventually(spec: ThenSpec<T>): Unit = thenEventually(10.seconds, spec)

    fun <T> thenEventually(duration: Duration, spec: ThenSpec<T>) {
        runBlocking {
            with(spec) {
                KotestThen.thenEventually(duration, testContext(), extractor) {
                    invokeMatcher(this, matcher)
                    testContext().apply { onMatch(this@thenEventually) }
                }
            }
        }
    }

    fun <T> thenContinually(extractor: StateExtractor<T>, match: Matcher<T>): Unit = thenContinually(10.seconds, extractor, match)

    fun <T> thenContinually(duration: Duration, extractor: StateExtractor<T>, block: T.() -> Unit) {
        runBlocking {
            KotestThen.thenContinually(duration, testContext(), extractor, block)
        }
    }

    fun <T> thenContinually(duration: Duration, extractor: StateExtractor<T>, match: Matcher<T>) {
        runBlocking {
            KotestThen.thenContinually(duration, testContext(), extractor, match)
        }
    }

    fun <T> thenEventually(extractor: StateExtractor<T>, match: Matcher<T>): Unit = thenEventually(10.seconds, extractor, match)

    fun <T> thenEventually(duration: Duration, extractor: StateExtractor<T>, match: Matcher<T>) {
        thenEventually(ZERO, duration, 25.milliseconds, extractor, match)
    }

    fun <T> thenEventually(initialDelay: Duration = ZERO, duration: Duration = 10.seconds, interval: Duration = 25.milliseconds, extractor: StateExtractor<T>, match: Matcher<T>) {
        runBlocking {
            KotestThen.thenEventually(initialDelay, duration, interval, testContext(), extractor, match)
        }
    }

    fun <T> thenEventually(extractor: StateExtractor<T>, block: T.() -> Unit = {}): Unit = thenEventually(10.seconds, extractor, block)

    fun <T> thenEventually(duration: Duration, extractor: StateExtractor<T>, block: T.() -> Unit = {}) {
        thenEventually(ZERO, duration, 25.milliseconds, extractor, block)
    }

    fun <T> thenEventually(initialDelay: Duration = ZERO, duration: Duration = 10.seconds, interval: Duration = 25.milliseconds, extractor: StateExtractor<T>, block: T.() -> Unit = {}) {
        runBlocking {
            KotestThen.thenEventually(initialDelay, duration, interval, testContext(), extractor, block)
        }
    }

    fun <T> then(extractor: StateExtractorWithFixtures<T>, block: T.() -> Unit) {
        KotestThen.then(testContext(), extractor, block)
    }

    fun <T> then(extractor: StateExtractorWithFixtures<T>, match: Matcher<T>) {
        KotestThen.then(testContext(), extractor, match)
    }

    fun <T> and(extractor: StateExtractorWithFixtures<T>, block: T.() -> Unit) {
        then(extractor, block)
    }

    fun <T> and(extractor: StateExtractorWithFixtures<T>, match: Matcher<T>) {
        then(extractor, match)
    }

    fun <T> thenEventually(spec: ThenSpecWithFixtures<T>): Unit = thenEventually(10.seconds, spec)
    fun <T> andEventually(spec: ThenSpecWithFixtures<T>): Unit = thenEventually(10.seconds, spec)

    fun <T> thenEventually(duration: Duration, spec: ThenSpecWithFixtures<T>) {
        runBlocking {
            with(spec) {
                KotestThen.thenEventually(duration, testContext(), extractor) {
                    invokeMatcher(this, matcher)
                    testContext().apply { onMatch(this@thenEventually) }
                }
            }
        }
    }

    fun <T> thenContinually(extractor: StateExtractorWithFixtures<T>, match: Matcher<T>): Unit = thenContinually(10.seconds, extractor, match)

    fun <T> thenContinually(duration: Duration, extractor: StateExtractorWithFixtures<T>, block: T.() -> Unit) {
        runBlocking {
            KotestThen.thenContinually(duration, testContext(), extractor, block)
        }
    }

    fun <T> thenContinually(duration: Duration, extractor: StateExtractorWithFixtures<T>, match: Matcher<T>) {
        runBlocking {
            KotestThen.thenContinually(duration, testContext(), extractor, match)
        }
    }

    fun <T> thenEventually(extractor: StateExtractorWithFixtures<T>, match: Matcher<T>): Unit = thenEventually(10.seconds, extractor, match)

    fun <T> thenEventually(duration: Duration, extractor: StateExtractorWithFixtures<T>, match: Matcher<T>) {
        thenEventually(ZERO, duration, 25.milliseconds, extractor, match)
    }

    fun <T> thenEventually(initialDelay: Duration = ZERO, duration: Duration = 10.seconds, interval: Duration = 25.milliseconds, extractor: StateExtractorWithFixtures<T>, match: Matcher<T>) {
        runBlocking {
            KotestThen.thenEventually(initialDelay, duration, interval, testContext(), extractor, match)
        }
    }

    fun <T> thenEventually(extractor: StateExtractorWithFixtures<T>, block: T.() -> Unit = {}): Unit = thenEventually(10.seconds, extractor, block)

    fun <T> thenEventually(duration: Duration, extractor: StateExtractorWithFixtures<T>, block: T.() -> Unit = {}) {
        thenEventually(ZERO, duration, 25.milliseconds, extractor, block)
    }

    fun <T> thenEventually(initialDelay: Duration = ZERO, duration: Duration = 10.seconds, interval: Duration = 25.milliseconds, extractor: StateExtractorWithFixtures<T>, block: T.() -> Unit = {}) {
        runBlocking {
            KotestThen.thenEventually(initialDelay, duration, interval, testContext(), extractor, block)
        }
    }
}