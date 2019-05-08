package dev.kensa.parse;

import dev.kensa.util.Reflect;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static dev.kensa.parse.CacheState.NotCached;
import static dev.kensa.parse.CacheState.NullValue;
import static java.util.stream.Collectors.toMap;

public class CachingScenarioMethodAccessor {

    private final Object testInstance;
    private final Map<String, Map<String, Object>> valueCache;
    private final Map<String, Object> scenarioCache;

    public CachingScenarioMethodAccessor(Object testInstance, Set<String> scenarioNames) {
        this.testInstance = testInstance;
        this.valueCache = scenarioNames.stream()
                                       .collect(toMap(Function.identity(), s -> new HashMap<>()));

        this.scenarioCache = scenarioNames.stream()
                                          .collect(toMap(Function.identity(), s -> NotCached));

    }

    Optional<Object> valueOf(String scenarioName, String methodName) {
        Object cachedValue = null;

        if (valueCache.containsKey(scenarioName)) {
            cachedValue = valueCache.get(scenarioName)
                                    .compute(methodName, (mn, existing) -> {
                                        if (existing == null) {
                                            Object value = scenarioInstanceWithName(scenarioName);
                                            if (value != null && value != NullValue) {
                                                value = Reflect.invokeMethod(value, mn, Object.class);
                                            }
                                            return value == null ? NullValue : value;
                                        }
                                        return existing;
                                    });
        }

        return Optional.ofNullable(cachedValue == NullValue ? null : cachedValue);
    }

    private Object scenarioInstanceWithName(String scenarioName) {
        return scenarioCache.compute(scenarioName, (name, existing) -> {
            if (existing == NotCached) {
                Object value = Reflect.fieldValue(testInstance, name, Object.class);
                return value == null ? NullValue : value;
            }

            return existing;
        });
    }
}
