package dev.kensa.kotest.testsupport.collections

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult

/**
 * Builds a `Matcher<Collection<T>>` that passes when no element matches [matcher].
 *
 * Intended for negative assertions ("no captured event matches"): pair it with `then` or
 * `thenContinually` — under `thenEventually` a negative assertion passes trivially on the first
 * poll, before a late event can arrive.
 */
fun <T> noneMatching(matcher: Matcher<T>): Matcher<Collection<T>> = object : Matcher<Collection<T>> {
    override fun test(value: Collection<T>): MatcherResult {
        val matching = value.filter { matcher.test(it).passed() }
        return MatcherResult(
            matching.isEmpty(),
            { "expected no elements matching but found ${matching.size}: $matching" },
            { "expected at least one element matching, but none did" }
        )
    }
}
