package dev.kensa.function;

import dev.kensa.KensaException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CheckedFunctionTest {

    @Test
    void catchesCheckedExceptionAndWrapsWithRuntime() {
        Exception cause = new Exception("Boom!");

        KensaException exception = assertThrows(KensaException.class, () -> CheckedFunction.unchecked(o -> {throw cause;}).apply(cause));

        assertThat(exception).hasCause(cause);
    }

    @Test
    void returnsResultOfWrappedFunction() {
        Integer expected = 10;

        assertThat(CheckedFunction.unchecked(v -> v).apply(expected)).isEqualTo(expected);
    }
}