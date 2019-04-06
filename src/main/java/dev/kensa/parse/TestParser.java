package dev.kensa.parse;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import dev.kensa.KensaException;
import dev.kensa.util.NameValuePair;
import dev.kensa.util.SourceCodeIndex;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TestParser {

    private final Object testInstance;
    private final Method method;
    private final Object[] arguments;

    public TestParser(Object testInstance, Method method, Object[] arguments) {
        this.testInstance = testInstance;
        this.method = method;
        this.arguments = arguments;
    }

    public ParsedTest parse() {
        MethodDeclaration declaration = methodDeclarationFrom(method);
        ParameterParser parameterParser = new ParameterParser(declaration);
        Map<String, NameValuePair> parameters = parameterParser.parameters(arguments);

        Map<String, NameValuePair> fieldValues = new HashMap<>();
        fieldValues.putAll(parameters);

        MethodParser methodParser = new MethodParser(declaration, s -> Optional.ofNullable(fieldValues.get(s)));

        return new ParsedTest(
            parameters.values(),
            methodParser.sentences()
        );
    }

    private MethodDeclaration methodDeclarationFrom(Method method) {
        try {
            Class<?> clazz = method.getDeclaringClass();
            MethodCriteria criteria = new MethodCriteria(method.getName(), method.getParameterTypes());

            CompilationUnit cu = JavaParser.parse(sourcePathFor(clazz));
            return cu.accept(new SimpleMethodMatchingVisitor(), criteria);
        } catch (IOException e) {
            throw new KensaException("Unable to create MethodParserFactory", e);
        }
    }

    private Path sourcePathFor(Class<?> clazz) {
        return SourceCodeIndex.locate(clazz);
    }
}
