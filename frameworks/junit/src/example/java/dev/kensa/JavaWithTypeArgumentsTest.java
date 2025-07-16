package dev.kensa;

import dev.kensa.junit.KensaTest;
import org.junit.jupiter.api.Test;

public class JavaWithTypeArgumentsTest implements KensaTest {

    @Test
    void passingTest() {
        given(this.<String>aLiteralOf("aStringValue"));
    }

    private <T> GivensBuilder aLiteralOf(T aValue) {
        return (givens) -> {};
    }
}
