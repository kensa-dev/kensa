package dev.kensa.matcher

import java.util.function.Predicate

object Predicates {
    fun <T, R> equalTo(extractor: (T) -> R, expected: R): Predicate<T> = ExtractingPredicateWithTest(extractor, expected, { a: R, b: R -> a == b })

    private class ExtractingPredicateWithTest<T, R>(private val extractor: (T) -> R, private val expected: R, private val test: (R, R) -> Boolean) : Predicate<T> {
        override fun test(actual: T) = test(extractor(actual), expected)
    }
}