package dev.kensa.matcher

import org.assertj.core.api.Condition
import java.util.function.Predicate
import kotlin.reflect.KClass

object Conditions {
    fun <T, EXPECTED> equalTo(description: String, expected: EXPECTED, predicate: Predicate<T>): Condition<T> =
            Condition(predicate, "a $description of %%s", expected)

    fun <T, EXPECTED> equalTo(expected: EXPECTED, predicate: Predicate<T>): Condition<T> =
            Condition(predicate, "a ${unCamelClassOf(expected)} of %%s", expected)

    fun <T, EXPECTED> equalTo(extractor: (T) -> EXPECTED, expected: EXPECTED): Condition<T> =
            equalTo(expected, Predicates.equalTo(extractor, expected))

    @JvmStatic
    fun <T, EXPECTED> equalTo(description: String, extractor: (T) -> EXPECTED, expected: EXPECTED): Condition<T> =
            equalTo(description, expected, Predicates.equalTo(extractor, expected))

    fun <T, EXPECTED> matches(extractor: (T) -> EXPECTED, subCondition: Condition<EXPECTED>, expected: EXPECTED): Condition<T> =
            Condition(
                    Predicate { t: T -> subCondition.matches(extractor(t)) }, "a ${unCamelClassOf(expected)} matching %%s",
                    expected
            )

    private fun unCamel(camelCasedWords: String): String = camelCasedWords.split("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])").joinToString(separator = " ")

    private fun unCamel(c: KClass<*>): String = unCamel(c.simpleName!!)

    private fun unCamelClassOf(o: Any?): String = if (o == null) "Null Instance" else unCamel(o::class.simpleName!!)
}