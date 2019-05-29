package dev.kensa.parse;

import com.github.javaparser.ast.body.MethodDeclaration;
import dev.kensa.render.Renderers;
import dev.kensa.util.NamedValue;
import dev.kensa.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toSet;

public class TestParser {

    private final Object testInstance;
    private final Method method;
    private final Object[] arguments;
    private final Renderers renderers;
    private final MethodDeclarationProvider methodDeclarationProvider;
    private final Pattern keywordPattern;
    private final Pattern acronymPattern;

    public TestParser(
            Object testInstance, Method method, Object[] arguments, Renderers renderers, MethodDeclarationProvider methodDeclarationProvider, Pattern keywordPattern,
            Pattern acronymPattern
    ) {
        this.testInstance = testInstance;
        this.method = method;
        this.arguments = arguments;
        this.renderers = renderers;
        this.methodDeclarationProvider = methodDeclarationProvider;
        this.keywordPattern = keywordPattern;
        this.acronymPattern = acronymPattern;
    }

    public ParsedTest parse() {
        MethodDeclaration declaration = methodDeclarationProvider.methodDeclarationFrom(method);
        ParameterCollector parameterCollector = new ParameterCollector(declaration);
        Set<NamedValue> parameters = parameterCollector.collect(arguments);

        CachingScenarioMethodAccessor scenarioAccessor = ReflectionUtil.scenarioAccessorFor(testInstance);
        CachingFieldAccessor fieldAccessor = ReflectionUtil.interestingFieldsOf(testInstance);
        ParameterAccessor parameterAccessor = new ParameterAccessor(parameters);

        Set<NamedValue> highlightedNamedValues = highlightedValuesOf(testInstance);

        ValueAccessors valueAccessors = new ValueAccessors(renderers, scenarioAccessor, fieldAccessor, parameterAccessor);

        Set<String> highlightedValues = highlightedNamedValues.stream()
                                                              .map(nv -> renderers.renderValueOnly(nv.value()))
                                                              .collect(toSet());

        MethodParser methodParser = new MethodParser(declaration, valueAccessors, highlightedValues, keywordPattern, acronymPattern);

        return new ParsedTest(parameters, methodParser.sentences(), highlightedNamedValues);
    }

    private Set<NamedValue> highlightedValuesOf(Object testInstance) {
        return ReflectionUtil.highlightedFieldsOf(testInstance)
                             .stream()
                             .map(nv -> new NamedValue(nv.name(), ReflectionUtil.fieldValue(testInstance, nv.value().toString(), Object.class))
                             )
                             .collect(toSet());
    }
}
