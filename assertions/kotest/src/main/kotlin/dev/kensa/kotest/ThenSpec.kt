package dev.kensa.kotest

import dev.kensa.CollectorContext
import dev.kensa.StateCollector
import io.kotest.matchers.Matcher

@Deprecated("Use ThenSpec instead", ReplaceWith("dev.kensa.kotest.ThenSpec<T>"))
typealias CollectingThenSpec<T> = ThenSpec<T>

interface ThenSpec<T> {
    val collector: StateCollector<T>
    val matcher: Matcher<T>
    val onMatch: CollectorContext.(T) -> Unit
        get() = {}
}