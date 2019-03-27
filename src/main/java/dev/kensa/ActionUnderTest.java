package dev.kensa;

import dev.kensa.state.CapturedInteractions;
import dev.kensa.state.Givens;

@FunctionalInterface
public interface ActionUnderTest {
    void execute(Givens givens, CapturedInteractions interactions);
}
