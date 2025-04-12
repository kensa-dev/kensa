package dev.kensa.example;

import dev.kensa.Highlight;
import dev.kensa.Scenario;
import dev.kensa.SentenceValue;

public class JavaWithInterface implements JavaInterface {

    private String field1;

    @Scenario
    private String field2;

    @Highlight
    @SentenceValue
    private String field3;

    void classTestMethod() {
        assertThat("xyz").contains("x");
    }

    private String assertThat(String actual) {
        return actual;
    }
}