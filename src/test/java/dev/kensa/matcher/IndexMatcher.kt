package dev.kensa.matcher

import dev.kensa.matcher.Conditions.equalTo
import dev.kensa.sentence.TokenType
import dev.kensa.sentence.scanner.Index
import org.assertj.core.api.Assertions
import org.assertj.core.api.Condition

object IndexMatcher {
    fun theSameAs(expected: Index): Condition<Index> =
            Assertions.allOf(
                    aTypeOf(expected.type),
                    aStartOf(expected.start),
                    anEndOf(expected.end)
            )

    fun aTypeOf(expected: TokenType) = equalTo("Type", Index::type, expected)

    fun aStartOf(expected: Int) = equalTo("Start Index", Index::start, expected)

    fun anEndOf(expected: Int) = equalTo("End Index", Index::end, expected)
}