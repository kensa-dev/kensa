package dev.kensa.parse;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import dev.kensa.function.Unchecked;
import dev.kensa.util.SourceCodeIndex;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class MethodDeclarationProvider {

    private final Map<Class<?>, CompilationUnit> cuCache = new ConcurrentHashMap<>();
    private final Map<Method, MethodDeclaration> mdCache = new ConcurrentHashMap<>();
    private final SimpleMethodMatchingVisitor visitor = new SimpleMethodMatchingVisitor();

    MethodDeclaration methodDeclarationFrom(Method method) {
        return mdCache.computeIfAbsent(method, methodDeclarationFactory());
    }

    private Path sourcePathFor(Class<?> clazz) {
        return SourceCodeIndex.locate(clazz);
    }

    private Function<Method, MethodDeclaration> methodDeclarationFactory() {
        return m -> cuCache.computeIfAbsent(m.getDeclaringClass(), Unchecked.function(c -> JavaParser.parse(sourcePathFor(c))))
                           .accept(visitor, new MethodCriteria(m.getName(), m.getParameterTypes()));
    }
}