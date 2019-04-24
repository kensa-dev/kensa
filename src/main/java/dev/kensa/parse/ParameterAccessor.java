package dev.kensa.parse;

import dev.kensa.util.NameValuePair;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static dev.kensa.parse.CacheState.NotCached;
import static dev.kensa.parse.CacheState.NullValue;
import static java.util.stream.Collectors.toMap;

class ParameterAccessor {

    private final Map<String, Object> parameters;

    ParameterAccessor(Set<NameValuePair> parameters) {
        this.parameters = parameters.stream().collect(toMap(NameValuePair::name, nvp -> nvp.value() == null ? NullValue : nvp.value()));
    }

    Optional<Object> valueOf(String parameterName) {
        return Optional.ofNullable(parameters.getOrDefault(parameterName, NotCached));
    }
}
