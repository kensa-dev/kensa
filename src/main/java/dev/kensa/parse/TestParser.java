package dev.kensa.parse;

import com.github.javaparser.ast.body.MethodDeclaration;
import dev.kensa.render.Renderers;
import dev.kensa.util.NameValuePair;
import dev.kensa.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

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
        ParameterParser parameterParser = new ParameterParser(declaration);
        Map<String, NameValuePair> parameters = parameterParser.parameters(arguments);

        Map<String, NameValuePair> fieldValues = ReflectionUtil.interestingFieldValuesOf(testInstance);
        fieldValues.putAll(parameters);

        MethodParser methodParser = new MethodParser(declaration, renderers, s -> Optional.ofNullable(fieldValues.get(s)));

        return new ParsedTest(
                parameters.values(),
                methodParser.sentences()
        );
    }
}
