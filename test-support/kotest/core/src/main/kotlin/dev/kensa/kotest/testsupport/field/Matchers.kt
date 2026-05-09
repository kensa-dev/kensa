package dev.kensa.kotest.testsupport.field

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult

/**
 * Builds a `Matcher<T?>` that extracts a value via [extractValue] and delegates to [match],
 * prefixing both failure messages with [name].
 *
 * Building block for all [MatcherField] extension functions; exposed for users writing their own
 * field-aware matchers.
 */
fun <T : Any, R> nullableExtractingMatcher(name: String, extractValue: (T) -> R?, match: Matcher<R?>): Matcher<T?> = object : Matcher<T?> {
    override fun test(value: T?): MatcherResult {
        val testResult = match.test(value?.let(extractValue))
        return MatcherResult(
            testResult.passed(),
            { "$name: ${testResult.failureMessage()}" },
            { "$name: ${testResult.negatedFailureMessage()}" }
        )
    }
}
