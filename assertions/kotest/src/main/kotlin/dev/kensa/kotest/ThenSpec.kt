package dev.kensa.kotest

import dev.kensa.StateExtractor
import dev.kensa.StateExtractorWithFixtures
import dev.kensa.fixture.Fixtures
import io.kotest.matchers.Matcher

interface ThenSpec<T> {
    val extractor: StateExtractor<T>
    val matcher: Matcher<T>
    val onMatch: (T) -> Unit
        get() = {}
}

interface ThenSpecWithFixtures<T> {
    val extractor: StateExtractorWithFixtures<T>
    val matcher: Matcher<T>
    val onMatch: (T, Fixtures) -> Unit
        get() = { _, _ -> }
}