package dev.kensa.kotest

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult

fun <T : Any, R> extractingMatcher(name: String, extractValue: (T) -> R, match: Matcher<R>): Matcher<T> = object : Matcher<T> {
    override fun test(value: T): MatcherResult {
        val testResult = match.test(extractValue(value))
        return MatcherResult(
            testResult.passed(),
            { "$name: ${testResult.failureMessage()}" },
            { "$name: ${testResult.negatedFailureMessage()}" }
        )
    }
}