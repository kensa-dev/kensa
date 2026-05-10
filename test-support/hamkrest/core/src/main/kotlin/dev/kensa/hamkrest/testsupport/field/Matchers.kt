package dev.kensa.hamkrest.testsupport.field

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher

/**
 * Builds a hamkrest `Matcher<T?>` that extracts a value via [extractValue] and delegates to
 * [matcher], prefixing the matcher's [description] and [negatedDescription][Matcher.negatedDescription]
 * with [name].
 *
 * Building block for all [MatcherField] extension functions; exposed for users writing their own
 * field-aware matchers.
 */
fun <T : Any, R> extractingMatcher(name: String, extractValue: (T) -> R?, matcher: Matcher<R?>): Matcher<T?> = object : Matcher<T?> {
    override val description: String get() = "$name: ${matcher.description}"
    override val negatedDescription: String get() = "$name: ${matcher.negatedDescription}"
    override fun invoke(actual: T?): MatchResult = matcher(actual?.let(extractValue))
}
