package dev.kensa.sentence.scanner;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static dev.kensa.matcher.IndexMatcher.theSameAs;
import static dev.kensa.sentence.Token.Type.Acronym;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class IndicesTest {

    @ParameterizedTest
    @MethodSource("testArguments")
    void handlesIndexAddition(List<Index> input, List<Index> expected) {
        Indices indices = new Indices();

        input.forEach(index -> indices.put(Acronym, index.start(), index.end()));

        List<Index> result = indices.stream()
                                    .collect(toList());

        assertThat(result).hasSize(expected.size());

        for (int index = 0; index < result.size(); index++) {
            assertThat(result.get(index)).is(theSameAs(expected.get(index)));
        }
    }

    static Stream<Arguments> testArguments() {
        return Stream.of(
                // Surrounding index added after
                arguments(
                        // Input
                        List.of(indexOf(1, 2), indexOf(0, 4)),
                        // Expected
                        List.of(indexOf(0, 4))
                ),
                // Surrounding index added before
                arguments(
                        // Input
                        List.of(indexOf(0, 4), indexOf(1, 2)),
                        // Expected
                        List.of(indexOf(0, 4))
                ),
                // Surrounding index added after multiple
                arguments(
                        // Input
                        List.of(indexOf(1, 2), indexOf(3, 4), indexOf(0, 4)),
                        // Expected
                        List.of(indexOf(0, 4))
                ),
                // Surrounding index added after multiple
                arguments(
                        // Input
                        List.of(indexOf(0, 4), indexOf(1, 2), indexOf(3, 4)),
                        // Expected
                        List.of(indexOf(0, 4))
                ),
                // Separate indices added in order
                arguments(
                        // Input
                        List.of(indexOf(0, 4), indexOf(5, 7), indexOf(10, 15)),
                        // Expected
                        List.of(indexOf(0, 4), indexOf(5, 7), indexOf(10, 15))
                ),
                // Separate indices added out of order
                arguments(
                        // Input
                        List.of(indexOf(5, 7), indexOf(10, 15), indexOf(0, 4)),
                        // Expected
                        List.of(indexOf(0, 4), indexOf(5, 7), indexOf(10, 15))
                ),
                // Surrounding indices kept when inner ones added after
                arguments(
                        // Input
                        List.of(indexOf(0, 3), indexOf(4, 7), indexOf(1, 2), indexOf(5, 6)),
                        // Expected
                        List.of(indexOf(0, 3), indexOf(4, 7))
                ),
                // Surrounding indices used when inner ones added before
                arguments(
                        // Input
                        List.of(indexOf(1, 2), indexOf(5, 6), indexOf(0, 3), indexOf(4, 7)),
                        // Expected
                        List.of(indexOf(0, 3), indexOf(4, 7))
                ),
                // Uses largest surrounding index
                arguments(
                        // Input
                        List.of(indexOf(0, 5), indexOf(0, 4)),
                        // Expected
                        List.of(indexOf(0, 5))
                ),
                // Uses largest surrounding index - reverse order
                arguments(
                        // Input
                        List.of(indexOf(0, 4), indexOf(0, 5)),
                        // Expected
                        List.of(indexOf(0, 5))
                ),
                // Uses earliest intersecting index
                arguments(
                        // Input
                        List.of(indexOf(0, 5), indexOf(1, 6)),
                        // Expected
                        List.of(indexOf(0, 5))
                )
        );
    }

    private static Index indexOf(int i, int i2) {
        return new Index(Acronym, i, i2);
    }
}