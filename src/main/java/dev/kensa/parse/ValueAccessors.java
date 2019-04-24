package dev.kensa.parse;

import dev.kensa.render.Renderers;

import java.util.Optional;

import static dev.kensa.parse.CacheState.NotCached;

public class ValueAccessors {

    private final Renderers renderers;
    private final CachingScenarioMethodAccessor scenarioAccessor;
    private final CachingFieldAccessor fieldAccessor;
    private final ParameterAccessor parameterAccessor;

    public ValueAccessors(Renderers renderers, CachingScenarioMethodAccessor scenarioAccessor, CachingFieldAccessor fieldAccessor, ParameterAccessor parameterAccessor) {
        this.renderers = renderers;
        this.scenarioAccessor = scenarioAccessor;
        this.fieldAccessor = fieldAccessor;
        this.parameterAccessor = parameterAccessor;
    }

    Optional<String> realValueOf(String scenarioName, String methodName) {
        return scenarioAccessor.valueOf(scenarioName, methodName)
                               .map(renderers::render);
    }

    Optional<String> realValueOf(String identifier) {
        return parameterOrFieldValue(identifier)
                .map(renderers::render);
    }

    private Optional<Object> parameterOrFieldValue(String identifier) {
        return parameterAccessor.valueOf(identifier)
                .flatMap(v ->  v == NotCached ? fieldAccessor.valueOf(identifier) : Optional.of(v));
    }
}
