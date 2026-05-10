package dev.kensa.hamkrest.testsupport.field

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.allOf
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat

/**
 * Composes two matchers — both must pass. Reads as English at the call-site.
 *
 * ```
 * (aProviderCode of "FW") with (aServiceType of "FTTP")
 * ```
 */
infix fun <T> Matcher<T>.with(other: Matcher<T>): Matcher<T> = this and other

/** Identity wrapper that lets the call-site read like an English phrase. */
fun <T> thatHas(matcher: Matcher<T>): Matcher<T> = matcher

/**
 * Composes multiple matchers — all must pass. Reads as English at the call-site.
 *
 * ```
 * assertThat(response, thatHas(
 *     aProviderCode of "FW",
 *     aServiceType of "FTTP",
 *     aDate of "2026-01-01"
 * ))
 * ```
 */
fun <T> thatHas(matcher: Matcher<T>, vararg matchers: Matcher<T>): Matcher<T> = allOf(matcher, *matchers)

/** Identity wrapper that lets the call-site read like an English phrase. */
fun <T> thatIs(matcher: Matcher<T>): Matcher<T> = matcher

/**
 * Asserts that the receiver passes every supplied matcher (composed via [allOf]).
 *
 * ```
 * response.shouldHaveAll(
 *     aProviderCode of "FW",
 *     aServiceType of "FTTP"
 * )
 * ```
 */
fun <T> T?.shouldHaveAll(vararg matchers: Matcher<T?>) = assertThat(this, allOf(*matchers))
