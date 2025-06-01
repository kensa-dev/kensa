package dev.kensa.example;

import dev.kensa.Highlight;
import dev.kensa.Resolve;

public class JavaWithInterface implements JavaInterface {

    private String field1;

    @Resolve
    private String field2;

    @Highlight
    @Resolve
    private String field3;

    void classTestMethod() {
        assertThat("xyz").contains("x");
    }

    private String assertThat(String actual) {
        return actual;
    }
}