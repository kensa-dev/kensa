package dev.kensa;

import dev.kensa.hamcrest.WithHamcrest;
import dev.kensa.junit.KensaTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

public class JavaWithGenericParameterizedTest implements KensaTest, WithHamcrest {

    @RenderedValue
    private final String aValue = "aStringValue";

    @ParameterizedTest
    @MethodSource("genericParameters")
    void theTest(List<Map<String, String>> param) {
        assertThat(param.get(0), hasKey("a"));
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> genericParameters() {
        return Stream.of(
                Arguments.of(List.of(Map.of("a", "b")))
        );
    }
}
