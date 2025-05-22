package dev.kensa.example;

import dev.kensa.*;

import static dev.kensa.DummyAssert.assertThat;
import static dev.kensa.SomeBuilder.someBuilder;

public class JavaWithVariousFields {

    private static final String MY_PARAMETER_VALUE = "myParameterValue";

    private String field1;

    @Scenario
    private String field2;

    @Highlight
    @SentenceValue
    private String field3;

    void simpleTest() {
        assertThat("string").isNotBlank();
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
}