package dev.kensa;

import dev.kensa.state.Givens;

@FunctionalInterface
public interface GivensBuilder {
     void build(Givens givens);
}