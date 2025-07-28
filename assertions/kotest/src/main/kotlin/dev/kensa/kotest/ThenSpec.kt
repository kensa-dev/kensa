package dev.kensa.kotest

import dev.kensa.CollectorContext
import dev.kensa.StateCollector
import io.kotest.matchers.Matcher

@Deprecated("Use CollectingThenSpec instead", ReplaceWith("CollectingThenSpec<T>"))
typealias ThenSpec<T> = CollectingThenSpec<T>

interface CollectingThenSpec<T> {
    val collector: StateCollector<T>
    val matcher: Matcher<T>
    val onMatch: CollectorContext.(T) -> Unit
        get() = {}
}