package dev.kensa.example;

import dev.kensa.ExpandableSentence;
import dev.kensa.GivensBuilder;
import dev.kensa.Highlight;
import dev.kensa.RenderedValue;

import static dev.kensa.DummyAssert.assertThat;
import static dev.kensa.SomeBuilder.someBuilder;

public class JavaWithVariousFields {

    private static final String MY_PARAMETER_VALUE = "myParameterValue";

    private String field1;

    @RenderedValue
    private String field2;

    @Highlight
    @RenderedValue
    private String field3;

    void simpleTest() {
        assertThat("string").isNotBlank();
    }

    @RenderedValue
    private void method1() {
    }

    @ExpandableSentence
    private GivensBuilder nested1() {
        return someBuilder()
                .withSomething()
                .build();
    }
}