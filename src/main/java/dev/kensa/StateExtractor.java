package dev.kensa;

import dev.kensa.state.CapturedInteractions;

@FunctionalInterface
public interface StateExtractor<T> {
    T execute(CapturedInteractions interactions);
}
