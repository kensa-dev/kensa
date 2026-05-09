package dev.kensa.kotest.testsupport.field

import io.kotest.matchers.Matcher
import io.kotest.matchers.be
import io.kotest.matchers.collections.containExactly
import io.kotest.matchers.collections.containExactlyInAnyOrder
import io.kotest.matchers.compose.all
import io.kotest.matchers.string.match

/**
 * A named field of a subject of type [T] that extracts a value of type [R] for matcher composition.
 *
 * Implementations capture *where* a field lives (e.g. an XPath, a JSONPointer) and *how* to
 * extract its value, so that extension functions ([of], [matching], [withListOf], [withSetOf])
 * can produce [Matcher]s that read like English at the test call-site:
 *
 * ```
 * aProviderCode of "FW",
 * aProviderCode matching "[A-Z]+",
 * lineItems.withListOf("a", "b", "c")
 * ```
 *
 * Failure messages produced by all extension functions are prefixed with [description].
 */
interface MatcherField<T, R> {
    /** Identifier of the field; basis for [description]. */
    val name: String

    /** Extracts the value of this field from [value], or `null` if absent. */
    fun extract(value: T): R?

    /**
     * Human-readable form of [name] used as the prefix in failure messages.
     *
     * Default derivation: split [name] on camelCase boundaries, drop leading articles
     * (`a` / `an` / `the`, case-insensitive), drop a trailing `Field`, join with spaces.
     * e.g. `aProviderCode` → `Provider Code`, `aFooField` → `Foo`.
     */
    val description get() = name.camelCaseSplit().dropWhile { articles.contains(it.lowercase()) }.dropLastWhile { it == "Field" }.joinToString(" ")
}

private val articles = setOf("a", "an", "the")

private val camelCaseSplitter = "(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])|(?<=[a-z])(?=[0-9])|(?<=[0-9])(?=[A-Z])".toRegex()

internal fun String.camelCaseSplit(): List<String> = camelCaseSplitter.split(this)

/**
 * Returns a matcher that passes when this field's extracted value equals [expected].
 *
 * ```
 * aProviderCode of "FW"
 * ```
 */
infix fun <T : Any, R> MatcherField<T, R>.of(expected: R?): Matcher<T?> = toMatcher(expected) { it }

/**
 * Returns a matcher that applies [matcher] to this field's extracted value.
 *
 * ```
 * anAmount matching beGreaterThan(0)
 * ```
 */
infix fun <T : Any, R> MatcherField<T, R>.matching(matcher: Matcher<R?>): Matcher<T?> =
    nullableExtractingMatcher(description, extractorThatThrows(), matcher)

/**
 * Returns a matcher that passes only when every supplied matcher passes against this field's
 * extracted value (composed via [Matcher.all]).
 */
fun <T : Any, R> MatcherField<T, R>.matching(matcher: Matcher<R?>, vararg others: Matcher<R?>): Matcher<T?> =
    nullableExtractingMatcher(description, extractorThatThrows(), Matcher.all(matcher, *others))

/**
 * Returns a matcher that passes when this field's extracted [String] matches [expectedRegex].
 *
 * ```
 * aSku matching "[A-Z]{3}-\\d+"
 * ```
 */
infix fun <T : Any> MatcherField<T, String>.matching(expectedRegex: String): Matcher<T?> =
    nullableExtractingMatcher(description, extractorThatThrows(), match(expectedRegex))

/**
 * Returns a matcher that passes when this field's extracted [List] equals [expected] in
 * exact order.
 */
fun <T : Any, R> MatcherField<T, List<R>>.withListOf(vararg expected: R): Matcher<T?> =
    nullableExtractingMatcher(description, { extract(it) }, containExactly(expected.asList()))

/**
 * Returns a matcher that passes when this field's extracted [Set] equals the elements of
 * [expected] (order-insensitive).
 */
fun <T : Any, R> MatcherField<T, Set<R>>.withSetOf(vararg expected: R): Matcher<T?> =
    nullableExtractingMatcher(description, { extract(it) }, containExactlyInAnyOrder(expected.toSet()))

/**
 * Adapter for users who want their own infix functions on [MatcherField] for domain value types:
 * applies [convertActual] to the extracted value before equality with [expected]. Exceptions
 * thrown by `extract` are wrapped with the field [description].
 *
 * ```
 * infix fun <T : Any> MatcherField<T, ProviderCode>.withValue(expected: String) =
 *     toMatcher(expected) { it.code }
 * ```
 */
fun <T : Any, R, OUT> MatcherField<T, R>.toMatcher(expected: OUT?, convertActual: (R) -> OUT?): Matcher<T?> =
    nullableExtractingMatcher(description, { extractorThatThrows()(it)?.let(convertActual) }, be(expected))

private fun <R, T : Any> MatcherField<T, R>.extractorThatThrows(): (T) -> R? = {
    try {
        extract(it)
    } catch (e: Exception) {
        throw RuntimeException("Failed to extract $description due to exception:", e)
    }
}
