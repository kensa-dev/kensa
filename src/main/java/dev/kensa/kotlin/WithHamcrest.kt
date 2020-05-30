package dev.kensa.kotlin

import dev.kensa.StateExtractor
import dev.kensa.context.TestContextHolder.testContext
import org.hamcrest.Matcher

interface WithHamcrest {

    fun <T> then(extractor: StateExtractor<T?>, matcher: Matcher<in T?>) {
        testContext().then(extractor, matcher)
    }

    fun <T> and(extractor: StateExtractor<T?>, matcher: Matcher<in T?>) {
        then(extractor, matcher)
    }

}
