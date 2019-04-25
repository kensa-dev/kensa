package dev.kensa;

import dev.kensa.state.CapturedInteractions;
import dev.kensa.state.Givens;

@FunctionalInterface
public interface GivensWithInteractionsBuilder {
     void build(Givens givens, CapturedInteractions capturedInteractions);
}