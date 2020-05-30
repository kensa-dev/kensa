package dev.kensa.sentence.scanner

import dev.kensa.matcher.IndexMatcher.theSameAs
import dev.kensa.sentence.TokenType.Acronym
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class IndicesTest {
    @ParameterizedTest
    @MethodSource("testArguments")
    fun handlesIndexAddition(input: List<Index>, expected: List<Index>) {
        val result = Indices().run {
            input.forEach { (_, start, end) -> put(Acronym, start, end) }
            toList()
        }

        assertThat(result).hasSize(expected.size)
        for (index in result.indices) {
            assertThat(result[index]).`is`(theSameAs(expected[index]))
        }
    }

    companion object {
        @JvmStatic
        fun testArguments(): Stream<Arguments> {
            return Stream.of(
                    // Surrounding index added after
                    arguments(
                            // Input
                            listOf(indexOf(1, 2), indexOf(0, 4)),
                            // Expected
                            listOf(indexOf(0, 4))
                    ),
                    // Surrounding index added before
                    arguments(
                            // Input
                            listOf(indexOf(0, 4), indexOf(1, 2)),
                            // Expected
                            listOf(indexOf(0, 4))
                    ),
                    // Surrounding index added after multiple
                    arguments(
                            // Input
                            listOf(indexOf(1, 2), indexOf(3, 4), indexOf(0, 4)),
                            // Expected
                            listOf(indexOf(0, 4))
                    ),
                    // Surrounding index added after multiple
                    arguments(
                            // Input
                            listOf(indexOf(0, 4), indexOf(1, 2), indexOf(3, 4)),
                            // Expected
                            listOf(indexOf(0, 4))
                    ),
                    // Separate indices added in order
                    arguments(
                            // Input
                            listOf(indexOf(0, 4), indexOf(5, 7), indexOf(10, 15)),
                            // Expected
                            listOf(indexOf(0, 4), indexOf(5, 7), indexOf(10, 15))
                    ),
                    // Separate indices added out of order
                    arguments(
                            // Input
                            listOf(indexOf(5, 7), indexOf(10, 15), indexOf(0, 4)),
                            // Expected
                            listOf(indexOf(0, 4), indexOf(5, 7), indexOf(10, 15))
                    ),
                    // Surrounding indices kept when inner ones added after
                    arguments(
                            // Input
                            listOf(indexOf(0, 3), indexOf(4, 7), indexOf(1, 2), indexOf(5, 6)),
                            // Expected
                            listOf(indexOf(0, 3), indexOf(4, 7))
                    ),
                    // Surrounding indices used when inner ones added before
                    arguments(
                            // Input
                            listOf(indexOf(1, 2), indexOf(5, 6), indexOf(0, 3), indexOf(4, 7)),
                            // Expected
                            listOf(indexOf(0, 3), indexOf(4, 7))
                    ),
                    // Uses largest surrounding index
                    arguments(
                            // Input
                            listOf(indexOf(0, 5), indexOf(0, 4)),
                            // Expected
                            listOf(indexOf(0, 5))
                    ),
                    // Uses largest surrounding index - reverse order
                    arguments(
                            // Input
                            listOf(indexOf(0, 4), indexOf(0, 5)),
                            // Expected
                            listOf(indexOf(0, 5))
                    ),
                    // Uses earliest intersecting index
                    arguments(
                            // Input
                            listOf(indexOf(0, 5), indexOf(1, 6)),
                            // Expected
                            listOf(indexOf(0, 5))
                    )
            )
        }

        private fun indexOf(i: Int, i2: Int) = Index(Acronym, i, i2)
    }
}