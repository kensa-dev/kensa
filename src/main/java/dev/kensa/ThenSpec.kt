package dev.kensa

import io.kotest.matchers.Matcher

interface ThenSpec<T> {
    val extractor: StateExtractor<T>
    val matcher: Matcher<T>
    val onPass: (T) -> Unit
        get() = {}
}