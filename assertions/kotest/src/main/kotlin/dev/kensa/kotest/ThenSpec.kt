package dev.kensa.kotest

import dev.kensa.StateExtractor
import io.kotest.matchers.Matcher

interface ThenSpec<T> {
    val extractor: StateExtractor<T>
    val matcher: Matcher<T>
    val onMatch: (T) -> Unit
        get() = {}
}