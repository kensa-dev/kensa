package dev.kensa.kotest

import dev.kensa.CollectorContext
import dev.kensa.StateCollector
import io.kotest.matchers.Matcher

interface ThenSpec<T> {
    val collector: StateCollector<T>
    val matcher: Matcher<T>
    val onMatch: CollectorContext.(T) -> Unit
        get() = {}
}