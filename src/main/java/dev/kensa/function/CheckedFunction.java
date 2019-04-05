package dev.kensa.function;

import java.util.function.Function;

@FunctionalInterface
public interface CheckedFunction<T, R> {

    R apply(T t) throws Throwable;

    static <T, R> Function<T, R> unchecked(CheckedFunction<T, R> function) {
        return Unchecked.function(function);
    }
}