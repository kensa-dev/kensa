package dev.kensa.hamkrest

import com.natpryce.hamkrest.Matcher
import dev.kensa.CollectorContext
import dev.kensa.StateCollector

interface ThenSpec<T> {
    val collector: StateCollector<T>
    val matcher: Matcher<T>
    val onMatch: CollectorContext.(T) -> Unit
        get() = {}
}
