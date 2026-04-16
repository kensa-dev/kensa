package dev.kensa.hamcrest

import dev.kensa.StateCollector
import dev.kensa.context.TestContextHolder.testContext
import org.hamcrest.Matcher
import java.time.Duration

interface WithHamcrest {

    fun <T> then(extractor: StateCollector<T>, matcher: Matcher<in T>) {
        HamcrestThen.then(testContext(), extractor, matcher)
    }

    fun <T> and(extractor: StateCollector<T>, matcher: Matcher<in T>) {
        then(extractor, matcher)
    }

    fun <T> thenContinually(extractor: StateCollector<T>, matcher: Matcher<in T>) {
        HamcrestThen.thenContinually(testContext(), extractor, matcher)
    }

    fun <T> thenContinually(duration: Duration, extractor: StateCollector<T>, matcher: Matcher<in T>) {
        HamcrestThen.thenContinually(duration, testContext(), extractor, matcher)
    }

    fun <T> thenEventually(extractor: StateCollector<T>, matcher: Matcher<in T>) {
        HamcrestThen.thenEventually(testContext(), extractor, matcher)
    }

    fun <T> andEventually(extractor: StateCollector<T>, matcher: Matcher<in T>) {
        thenEventually(extractor, matcher)
    }

    fun <T> thenEventually(duration: Duration, extractor: StateCollector<T>, matcher: Matcher<in T>) {
        HamcrestThen.thenEventually(duration, testContext(), extractor, matcher)
    }

    fun <T> thenEventually(initialDelay: Duration, duration: Duration, interval: Duration, extractor: StateCollector<T>, matcher: Matcher<in T>) {
        HamcrestThen.thenEventually(initialDelay, duration, interval, testContext(), extractor, matcher)
    }
}
