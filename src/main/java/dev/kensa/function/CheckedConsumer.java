package dev.kensa.function;

import java.util.function.Consumer;

@FunctionalInterface
public interface CheckedConsumer<T> {

    void accept(T t) throws Throwable;

    static <T> Consumer<T> unchecked(CheckedConsumer<T> consumer) {
        return Unchecked.consumer(consumer);
    }
}