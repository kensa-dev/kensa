package dev.kensa;

import org.junit.jupiter.api.Test;

public class JavaWithTypeArgumentsTest extends JavaExampleTest {

    @Test
    void passingTest() {
        given(this.<String>aLiteralOf("aStringValue"));
    }

    private <T> GivensBuilder aLiteralOf(T aValue) {
        return (givens) -> {
        };
    }
}
