package dev.kensa.parse;

import com.github.javaparser.ast.body.MethodDeclaration;
import dev.kensa.render.Renderers;
import dev.kensa.state.TestInvocationContext;
import dev.kensa.util.NamedValue;
import dev.kensa.util.Reflect;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class TestParser {

    private final Object testInstance;
    private final Method method;
    private final Object[] arguments;
    private final Renderers renderers;
    private final MethodDeclarationProvider methodDeclarationProvider;
    private final Set<String> keywords;
    private final Set<String> acronyms;

    public TestParser(
            TestInvocationContext context,
            Renderers renderers,
            MethodDeclarationProvider methodDeclarationProvider,
            Set<String> keywords,
            Set<String> acronyms
    ) {
        this.testInstance = context.testInstance();
        this.method = context.testMethod();
        this.arguments = context.testParameters();
        this.renderers = renderers;
        this.methodDeclarationProvider = methodDeclarationProvider;
        this.keywords = keywords;
        this.acronyms = acronyms;
    }

    public ParsedTest parse() {
        MethodDeclaration declaration = methodDeclarationProvider.methodDeclarationFrom(method);
        ParameterCollector parameterCollector = new ParameterCollector(declaration);
        List<NamedValue> parameters = parameterCollector.collect(arguments);

        CachingScenarioMethodAccessor scenarioAccessor = Reflect.scenarioAccessorFor(testInstance);
        CachingFieldAccessor fieldAccessor = Reflect.interestingFieldsOf(testInstance);
        ParameterAccessor parameterAccessor = Reflect.interestingParametersOf(method, parameters);

        Set<NamedValue> highlightedNamedValues = highlightedValuesOf(testInstance);

        ValueAccessors valueAccessors = new ValueAccessors(renderers, scenarioAccessor, fieldAccessor, parameterAccessor);

        Set<String> highlightedValues = highlightedNamedValues.stream()
                                                              .map(nv -> renderers.renderValueOnly(nv.value()))
                                                              .collect(toSet());

        MethodParser methodParser = new MethodParser(declaration, valueAccessors, highlightedValues, keywords, acronyms);

        return new ParsedTest(parameters, methodParser.sentences(), highlightedNamedValues);
    }

    private Set<NamedValue> highlightedValuesOf(Object testInstance) {
        return Reflect.highlightedFieldsOf(testInstance)
                      .stream()
                      .map(nv -> new NamedValue(nv.name(), Reflect.fieldValue(testInstance, nv.value().toString(), Object.class)))
                      .collect(toSet());
    }
}
