package dev.kensa.assertj;

import dev.kensa.StateExtractor;

import java.util.Optional;

/**
 * @deprecated Use {@link OptionalStateCollector} instead
 */
@Deprecated
public interface OptionalStateExtractor<T> extends StateExtractor<Optional<T>> {
}
