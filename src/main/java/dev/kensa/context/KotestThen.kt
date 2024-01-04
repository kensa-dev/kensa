package dev.kensa.context

import dev.kensa.StateExtractor
import io.kotest.assertions.failure
import io.kotest.assertions.nondeterministic.*
import io.kotest.matchers.Matcher
import io.kotest.matchers.invokeMatcher
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object KotestThen {
    fun <T> then(testContext: TestContext, extractor: StateExtractor<T>, match: Matcher<T>) {
        then(testContext, extractor) {
            invokeMatcher(this, match)
        }
    }

    fun <T> then(testContext: TestContext, extractor: StateExtractor<T>, block: T.() -> Unit) {
        block(extractor.execute(testContext.interactions))
    }

    suspend fun <T> thenContinually(duration: Duration = 10.seconds, testContext: TestContext, extractor: StateExtractor<T>, match: Matcher<T>) {
        thenContinually(duration, testContext, extractor) {
            invokeMatcher(extractor.execute(testContext.interactions), match)
        }
    }

    suspend fun <T> thenContinually(duration: Duration = 10.seconds, testContext: TestContext, extractor: StateExtractor<T>, block: T.() -> Unit) {
        continually(duration) {
            block(extractor.execute(testContext.interactions))
        }
    }

    suspend fun <T> thenEventually(duration: Duration = 10.seconds, testContext: TestContext, extractor: StateExtractor<T>, match: Matcher<T>) {
        thenEventually(duration, testContext, extractor) {
            invokeMatcher(extractor.execute(testContext.interactions), match)
        }
    }

    suspend fun <T> thenEventually(duration: Duration = 10.seconds, testContext: TestContext, extractor: StateExtractor<T>, block: T.() -> Unit) {
        var lastThrowable: Throwable? = null
        val config = eventuallyConfig {
            this.duration = duration
            this.listener = { _, throwable -> lastThrowable = throwable }
        }

        try {
            eventually(config) {
                block(extractor.execute(testContext.interactions))
            }
        } catch (e: Throwable) {
            lastThrowable?.let {
                if (it is AssertionError) throw it
                else throw failure(it.message ?: "eventually block failed", it)
            } ?: throw e
        }
    }
}