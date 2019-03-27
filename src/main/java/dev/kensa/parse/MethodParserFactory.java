package dev.kensa.parse;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import dev.kensa.KensaException;
import dev.kensa.util.SourceCodeIndex;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;

public class MethodParserFactory {

    private final MethodDeclaration declaration;

    public MethodParserFactory(Method method) {
        try {
            var clazz = method.getDeclaringClass();
            var criteria = new MethodCriteria(method.getName(), method.getParameterTypes());

            var cu = JavaParser.parse(sourcePathFor(clazz));
            declaration = cu.accept(new SimpleMethodMatchingVisitor(), criteria);
        } catch (IOException e) {
            throw new KensaException("Unable to create MethodParserFactory", e);
        }
    }

    public MethodParser createFor(Object[] arguments) {
        return new MethodParser(declaration, arguments);
    }

    private Path sourcePathFor(Class<?> clazz) {
        return SourceCodeIndex.locate(clazz);
    }
}
