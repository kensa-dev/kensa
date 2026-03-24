package dev.kensa.example;

import dev.kensa.Action;
import dev.kensa.GivensContext;
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

    private <T> Action<GivensContext> aLiteralOf(T aValue) {
        return (context) -> {
        };
    }
}
