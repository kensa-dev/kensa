package dev.kensa.function;

import dev.kensa.KensaException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CheckedConsumerTest {

    @Test
    void catchesCheckedExceptionAndWrapsWithRuntime() {
        Exception cause = new Exception("Boom!");

        KensaException exception = assertThrows(KensaException.class, () -> CheckedConsumer.unchecked(o -> {throw cause;}).accept(cause));

        assertThat(exception).hasCause(cause);
    }

    @SuppressWarnings("unchecked")
    @Test
    void delegatesToWrappedConsumer() throws Throwable {
        CheckedConsumer<String> consumer = mock(CheckedConsumer.class);

        CheckedConsumer.unchecked(consumer).accept("abc");

        verify(consumer).accept("abc");
    }
}