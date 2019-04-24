package dev.kensa.parse;

import com.github.javaparser.ast.body.MethodDeclaration;
import dev.kensa.render.Renderers;
import dev.kensa.util.NameValuePair;
import dev.kensa.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.Set;

public class TestParser {

    private final Object testInstance;
    private final Method method;
    private final Object[] arguments;
    private final Renderers renderers;
    private final MethodDeclarationProvider methodDeclarationProvider;

    public TestParser(Object testInstance, Method method, Object[] arguments, Renderers renderers, MethodDeclarationProvider methodDeclarationProvider) {
        this.testInstance = testInstance;
        this.method = method;
        this.arguments = arguments;
        this.renderers = renderers;
        this.methodDeclarationProvider = methodDeclarationProvider;
    }

    public ParsedTest parse() {
        MethodDeclaration declaration = methodDeclarationProvider.methodDeclarationFrom(method);
        ParameterCollector parameterCollector = new ParameterCollector(declaration);
        Set<NameValuePair> parameters = parameterCollector.collect(arguments);

        CachingScenarioMethodAccessor scenarioAccessor = ReflectionUtil.scenarioAccessorFor(testInstance);
        CachingFieldAccessor fieldAccessor = ReflectionUtil.interestingFieldsOf(testInstance);
        ParameterAccessor parameterAccessor = new ParameterAccessor(parameters);

        ValueAccessors valueAccessors = new ValueAccessors(renderers, scenarioAccessor, fieldAccessor, parameterAccessor);

        MethodParser methodParser = new MethodParser(declaration, valueAccessors);

        return new ParsedTest(parameters, methodParser.sentences());
    }
}
