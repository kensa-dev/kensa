package dev.kensa.kotest

import dev.kensa.CollectorContext
import dev.kensa.StateCollector
import dev.kensa.StateExtractor
import dev.kensa.StateExtractorWithFixtures
import dev.kensa.WithFixtures
import io.kotest.matchers.Matcher
import kotlin.DeprecationLevel.WARNING

interface ThenSpec<T> {
    val extractor: StateExtractor<T>
    val matcher: Matcher<T>
    val onMatch: WithFixtures.(T) -> Unit
        get() = {}
}

interface CollectingThenSpec<T> {
    val collector: StateCollector<T>
    val matcher: Matcher<T>
    val onMatch: CollectorContext.(T) -> Unit
        get() = {}
}

@Deprecated("use ThenSpec<StateCollector> instead", ReplaceWith(""), WARNING)
interface ThenSpecWithFixtures<T> {
    val extractor: StateExtractorWithFixtures<T>
    val matcher: Matcher<T>
    val onMatch: WithFixtures.(T) -> Unit
        get() = {}
}