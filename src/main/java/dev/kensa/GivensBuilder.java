package dev.kensa;

import dev.kensa.state.CapturedInteractions;
import dev.kensa.state.Givens;

@FunctionalInterface
public interface GivensBuilder {
     default void build(Givens givens, CapturedInteractions interactions) {
          build(givens);
     }

     void build(Givens givens);
}