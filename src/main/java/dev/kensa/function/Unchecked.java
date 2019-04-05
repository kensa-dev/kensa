package dev.kensa.function;

import dev.kensa.KensaException;

import java.util.function.Consumer;
import java.util.function.Function;

public final class Unchecked {

    public static <T> Consumer<T> consumer(CheckedConsumer<T> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Throwable e) {
                throw new KensaException(e);
            }
        };
    }

    public static <T, R> Function<T, R> function(CheckedFunction<T, R> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Throwable e) {
                throw new KensaException(e);
            }
        };
    }

    private Unchecked() {}
}
