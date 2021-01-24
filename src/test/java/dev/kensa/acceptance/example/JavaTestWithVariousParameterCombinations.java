package dev.kensa.acceptance.example;

import dev.kensa.Highlight;
import dev.kensa.NestedSentence;
import dev.kensa.Scenario;
import dev.kensa.SentenceValue;
import dev.kensa.java.JavaKensaTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static dev.kensa.acceptance.example.TestExtension.MY_PARAMETER_VALUE;
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

    @SentenceValue
    private void method1() {
    }

    @NestedSentence
    private void nested1() {
    }
}
