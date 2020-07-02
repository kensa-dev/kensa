package dev.kensa.parse;

import dev.kensa.util.DisplayableNamedValue;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static dev.kensa.parse.CacheState.NotCached;
import static dev.kensa.parse.CacheState.NullValue;
import static java.util.stream.Collectors.toMap;

public class ParameterAccessor {

    private final Map<String, Object> parameters;

    public ParameterAccessor(Set<DisplayableNamedValue> parameters) {
        this.parameters = parameters.stream().collect(toMap(DisplayableNamedValue::name, parameter -> parameter.value() == null ? NullValue : parameter.value()));
    }

    Optional<Object> valueOf(String parameterName) {
        return Optional.ofNullable(parameters.getOrDefault(parameterName, NotCached));
    }
}
