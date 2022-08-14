package dev.kensa.example;

import dev.kensa.Highlight;
import dev.kensa.Scenario;
import dev.kensa.SentenceValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaTestFromInterface implements JavaTestInterface {

    private String field1;

    @Scenario
    private String field2;

    @Highlight
    @SentenceValue
    private String field3;

    @Test
    void classTestMethod() {
        assertThat("xyz").contains("x");
    }
}