package dev.kensa.parse;

import dev.kensa.util.NamedValue;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static dev.kensa.parse.CacheState.NotCached;
import static dev.kensa.parse.CacheState.NullValue;
import static java.util.stream.Collectors.toMap;

public class ParameterAccessor {

    private final Map<String, Object> parameters;

    public ParameterAccessor(Set<NamedValue> parameters) {
        this.parameters = parameters.stream().collect(toMap(NamedValue::name, nv -> nv.value() == null ? NullValue : nv.value()));
    }

    Optional<Object> valueOf(String parameterName) {
        return Optional.ofNullable(parameters.getOrDefault(parameterName, NotCached));
    }
}
