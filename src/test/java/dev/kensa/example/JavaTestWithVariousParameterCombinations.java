package dev.kensa.example;

import dev.kensa.*;
import dev.kensa.java.JavaKensaTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static dev.kensa.example.JavaTestWithVariousParameterCombinations.SomeBuilder.someBuilder;
import static dev.kensa.example.TestExtension.MY_PARAMETER_VALUE;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({TestExtension.class})
public class JavaTestWithVariousParameterCombinations implements JavaKensaTest {

    private String field1;

    @Scenario
    private String field2;

    @Highlight
    @SentenceValue
    private String field3;

    @Test
    void similarNameTest() {
        assertThat("true").isEqualTo("true");
    }

    @Test
    void similarNameTest1() {
        assertThat("string").isNotBlank();
    }

    @Test
    void testWithNoParameters() {
        assertThat("true").isEqualTo("true");
    }

    @Test
    void testWithExtensionParameter(TestExtension.MyArgument first) {
        assertThat(first.getValue()).isEqualTo(MY_PARAMETER_VALUE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "b"})
    void parameterizedTest(String first) {
        assertThat(first).isIn("a", "b");
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "b"})
    void parameterizedTestWithExtensionParameter(String first, @SentenceValue TestExtension.MyArgument second) {
        assertThat(first).isIn("a", "b");
        assertThat(second.getValue()).isEqualTo(MY_PARAMETER_VALUE);
    }

    @ParameterizedTest
    @MethodSource("genericParameters")
    void testWithGenericExtensionParameter(List<Map<String, String>> param) {
        assertThat(param.get(0)).containsKey("a");
    }

    @ParameterizedTest
    @ValueSource(ints = {1})
    void parameterizedTestWithPrimitiveParameter(int param) {
        assertThat(param).isEqualTo(1);
    }

    @SentenceValue
    private void method1() {
    }

    @NestedSentence
    private GivensBuilder nested1() {
        return someBuilder()
                .withSomething()
                .build();
    }

    public static class SomeBuilder {
        public static SomeBuilder someBuilder() {
            return new SomeBuilder();
        }

        public SomeBuilder withSomething() {
            return this;
        }

        public GivensBuilder build() {
            return (givens) -> givens.put("notImportant", "");
        }
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> genericParameters() {
        return Stream.of(
                Arguments.of(List.of(Map.of("a", "b")))
        );
    }
}
