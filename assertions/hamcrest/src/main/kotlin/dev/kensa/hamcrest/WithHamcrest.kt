package dev.kensa.hamcrest

import dev.kensa.StateExtractor
import dev.kensa.StateExtractorWithFixtures
import dev.kensa.context.TestContext
import dev.kensa.context.TestContextHolder.testContext
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert

interface WithHamcrest {

    fun <T> then(extractor: StateExtractor<T?>, matcher: Matcher<in T?>) {
        testContext().then(extractor, matcher)
    }

    fun <T> and(extractor: StateExtractor<T?>, matcher: Matcher<in T?>) {
        then(extractor, matcher)
    }

    fun <T> then(extractor: StateExtractorWithFixtures<T?>, matcher: Matcher<in T?>) {
        testContext().then(extractor, matcher)
    }

    fun <T> and(extractor: StateExtractorWithFixtures<T?>, matcher: Matcher<in T?>) {
        then(extractor, matcher)
    }
}

private fun <T> TestContext.then(extractor: StateExtractor<T?>, matcher: Matcher<in T?>) {
    MatcherAssert.assertThat(extractor.execute(interactions), matcher)
}

private fun <T> TestContext.then(extractor: StateExtractorWithFixtures<T?>, matcher: Matcher<in T?>) {
    MatcherAssert.assertThat(extractor.execute(fixtures, interactions), matcher)
}
