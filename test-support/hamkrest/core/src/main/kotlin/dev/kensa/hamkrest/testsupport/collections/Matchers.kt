package dev.kensa.hamkrest.testsupport.collections

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher

/**
 * Builds a `Matcher<Collection<T>>` that passes when no element matches [matcher].
 *
 * Intended for negative assertions ("no captured event matches"): pair it with `then` or
 * `thenContinually` — under `thenEventually` a negative assertion passes trivially on the first
 * poll, before a late event can arrive.
 */
fun <T> noneMatching(matcher: Matcher<T>): Matcher<Collection<T>> = object : Matcher<Collection<T>> {
    override val description: String get() = "contains no element matching ${matcher.description}"
    override val negatedDescription: String get() = "contains at least one element matching ${matcher.description}"
    override fun invoke(actual: Collection<T>): MatchResult {
        val matching = actual.filter { matcher(it) == MatchResult.Match }
        return if (matching.isEmpty()) MatchResult.Match
        else MatchResult.Mismatch("expected no elements matching but found ${matching.size}: $matching")
    }
}
