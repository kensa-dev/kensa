package dev.kensa.parse;

import dev.kensa.Kensa;
import dev.kensa.render.Renderers;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class TestParserFactory {

    private final MethodDeclarationProvider methodDeclarationProvider;
    private final Pattern acronymPattern;
    private final Pattern keywordPattern;
    private final Renderers renderers;

    public TestParserFactory(Kensa.Configuration configuration, MethodDeclarationProvider methodDeclarationProvider) {
        this.acronymPattern = configuration.dictionary().acronymPattern();
        this.keywordPattern = configuration.dictionary().keywordPattern();
        this.renderers = configuration.renderers();
        this.methodDeclarationProvider = methodDeclarationProvider;
    }

    public TestParser create(Object testInstance, Method testMethod, Object[] arguments) {
        return new TestParser(
                testInstance,
                testMethod,
                arguments,
                renderers,
                methodDeclarationProvider,
                keywordPattern,
                acronymPattern
        );
    }
}
