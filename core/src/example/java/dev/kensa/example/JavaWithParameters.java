package dev.kensa.example;

import dev.kensa.*;

import static dev.kensa.DummyAssert.assertThat;

public class JavaWithParameters {

    private static final String MY_PARAMETER_VALUE = "myParameterValue";

    void methodWithParameters(String first, @RenderedValue String second) {
        assertThat(first).isIn("a", "b");
        assertThat(second).isEqualTo(MY_PARAMETER_VALUE);
    }
}