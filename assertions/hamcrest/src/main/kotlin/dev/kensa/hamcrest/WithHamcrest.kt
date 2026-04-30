package dev.kensa.hamcrest

import dev.kensa.StateCollector
import dev.kensa.context.TestContextHolder.testContext
import org.hamcrest.Matcher
import java.time.Duration

interface WithHamcrest {

    fun <T> then(collector: StateCollector<T>, matcher: Matcher<in T>) {
        HamcrestThen.then(testContext(), collector, matcher)
    }

    fun <T> and(collector: StateCollector<T>, matcher: Matcher<in T>) {
        then(collector, matcher)
    }

    fun <T> thenContinually(collector: StateCollector<T>, matcher: Matcher<in T>) {
        HamcrestThen.thenContinually(testContext(), collector, matcher)
    }

    fun <T> thenContinually(duration: Duration, collector: StateCollector<T>, matcher: Matcher<in T>) {
        HamcrestThen.thenContinually(duration, testContext(), collector, matcher)
    }

    fun <T> thenEventually(collector: StateCollector<T>, matcher: Matcher<in T>) {
        HamcrestThen.thenEventually(testContext(), collector, matcher)
    }

    fun <T> andEventually(collector: StateCollector<T>, matcher: Matcher<in T>) {
        thenEventually(collector, matcher)
    }

    fun <T> thenEventually(duration: Duration, collector: StateCollector<T>, matcher: Matcher<in T>) {
        HamcrestThen.thenEventually(duration, testContext(), collector, matcher)
    }

    fun <T> andEventually(duration: Duration, collector: StateCollector<T>, matcher: Matcher<in T>): Unit = thenEventually(duration, collector, matcher)

    fun <T> thenEventually(initialDelay: Duration, duration: Duration, interval: Duration, collector: StateCollector<T>, matcher: Matcher<in T>) {
        HamcrestThen.thenEventually(initialDelay, duration, interval, testContext(), collector, matcher)
    }

    fun <T> andEventually(initialDelay: Duration, duration: Duration, interval: Duration, collector: StateCollector<T>, matcher: Matcher<in T>): Unit =
        thenEventually(initialDelay, duration, interval, collector, matcher)
}
