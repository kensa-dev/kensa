package dev.kensa.parse;

import dev.kensa.util.Reflect;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static dev.kensa.parse.CacheState.NotCached;
import static java.util.stream.Collectors.toMap;

public class CachingFieldAccessor {

    private final Object testInstance;
    private final Map<String, Object> cachedValues;

    public CachingFieldAccessor(Object testInstance, Set<String> fieldNames) {
        this.testInstance = testInstance;
        this.cachedValues = fieldNames.stream()
                                      .collect(toMap(Function.identity(), s -> NotCached));
    }

    Optional<Object> valueOf(String fieldName) {
        return Optional.ofNullable(
                cachedValues.compute(fieldName, (fn, existing) -> {
                    if (existing == NotCached) {
                        return Reflect.fieldValue(testInstance, fieldName, Object.class);
                    }
                    return existing;
                })
        );
    }
}
