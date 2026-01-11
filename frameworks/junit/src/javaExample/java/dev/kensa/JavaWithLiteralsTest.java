package dev.kensa;

import org.junit.jupiter.api.Test;

public class JavaWithLiteralsTest extends JavaExampleTest {

    @Test
    void passingTest() {
        given(aLiteralOf("aStringValue"));
        given(aLiteralOf(true));
        given(aLiteralOf(10));
        given(aLiteralOf(null));
        given(aLiteralOf('a'));
        given(aLiteralOf("""
                a multiline string
                """));
    }

    private <T> GivensBuilder aLiteralOf(T aValue) {
        return (givens) -> {
        };
    }
}
