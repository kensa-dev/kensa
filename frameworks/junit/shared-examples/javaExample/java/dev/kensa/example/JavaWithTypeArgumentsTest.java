package dev.kensa.example;

import dev.kensa.Action;
import dev.kensa.GivensContext;
import org.junit.jupiter.api.Test;

public class JavaWithTypeArgumentsTest extends JavaExampleTest {

    @Test
    void passingTest() {
        given(this.<String>aLiteralOf("aStringValue"));
    }

    private <T> Action<GivensContext> aLiteralOf(T aValue) {
        return context -> {
        };
    }
}
