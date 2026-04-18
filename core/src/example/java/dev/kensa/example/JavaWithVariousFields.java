package dev.kensa.example;

import dev.kensa.*;
import org.jetbrains.annotations.NotNull;

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
    private @NotNull Action<GivensContext> expandable1() {
        return someBuilder()
                .withSomething()
                .build();
    }
}