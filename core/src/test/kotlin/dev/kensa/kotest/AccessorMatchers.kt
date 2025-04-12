package dev.kensa.kotest

import dev.kensa.parse.Accessor.ValueAccessor.ParameterAccessor
import dev.kensa.parse.Accessor.ValueAccessor.MethodAccessor
import dev.kensa.parse.Accessor.ValueAccessor.PropertyAccessor
import dev.kensa.sentence.scanner.Index
import io.kotest.assertions.withClue
import io.kotest.matchers.*

infix fun PropertyAccessor.shouldBe(expected: PropertyAccessor) = this should be(expected)

private fun be(expected: PropertyAccessor): Matcher<PropertyAccessor> =
    (propertyAccessorHas("name", PropertyAccessor::name, be(expected.name)))
        .and(propertyAccessorHas("isSentenceValue", PropertyAccessor::isSentenceValue, be(expected.isSentenceValue)))
        .and(propertyAccessorHas("isHighlight", PropertyAccessor::isHighlight, be(expected.isHighlight)))
        .and(propertyAccessorHas("isScenario", PropertyAccessor::isScenario, be(expected.isScenario)))

private fun <R> propertyAccessorHas(name: String, extractValue: (PropertyAccessor) -> R, match: Matcher<R>) = extractingMatcher(name, extractValue, match)

fun <R> PropertyAccessor.asClue(block: (PropertyAccessor) -> R) =
    withClue({ lazy { "PropertyAccessor :: [ name: $name,  isSentenceValue: $isSentenceValue, isHighlight: $isHighlight, isScenario: $isScenario]" }.value }) { block(this) }

infix fun MethodAccessor.shouldBe(expected: MethodAccessor) = this should be(expected)

private fun be(expected: MethodAccessor): Matcher<MethodAccessor> =
    (methodAccessorHas("name", MethodAccessor::name, be(expected.name)))
        .and(methodAccessorHas("isSentenceValue", MethodAccessor::isSentenceValue, be(expected.isSentenceValue)))
        .and(methodAccessorHas("isHighlight", MethodAccessor::isHighlight, be(expected.isHighlight)))
        .and(methodAccessorHas("isScenario", MethodAccessor::isScenario, be(expected.isScenario)))

private fun <R> methodAccessorHas(name: String, extractValue: (MethodAccessor) -> R, match: Matcher<R>) = extractingMatcher(name, extractValue, match)

infix fun ParameterAccessor.shouldBe(expected: ParameterAccessor) = this should be(expected)

private fun be(expected: ParameterAccessor): Matcher<ParameterAccessor> =
    (parameterAccessorHas("name", ParameterAccessor::name, be(expected.name)))
        .and(parameterAccessorHas("index", ParameterAccessor::index, be(expected.index)))
        .and(parameterAccessorHas("isSentenceValue", ParameterAccessor::isSentenceValue, be(expected.isSentenceValue)))
        .and(parameterAccessorHas("isHighlight", ParameterAccessor::isHighlight, be(expected.isHighlight)))

private fun <R> parameterAccessorHas(name: String, extractValue: (ParameterAccessor) -> R, match: Matcher<R>) = extractingMatcher(name, extractValue, match)

infix fun Index.shouldBe(expected: Index) = this should be(expected)

private fun be(expected: Index) : Matcher<Index> =
    (indexHas("Type", Index::type, be(expected.type)))
        .and(indexHas("Start Index", Index::start, be(expected.start)))
        .and(indexHas("End Index", Index::end, be(expected.end)))

private fun <R> indexHas(name: String, extractValue: (Index) -> R, match: Matcher<R>) = extractingMatcher(name, extractValue, match)