package dev.kensa.example;

import dev.kensa.Highlight;
import dev.kensa.RenderedValue;

public class JavaWithInterface implements JavaInterface {

    private String field1;

    @RenderedValue
    private String field2;

    @Highlight
    @RenderedValue
    private String field3;

    void classTestMethod() {
        assertThat("xyz").contains("x");
    }

    private String assertThat(String actual) {
        return actual;
    }
}