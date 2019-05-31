package dev.kensa.parse;

import dev.kensa.Kensa;
import dev.kensa.state.TestInvocationContext;

import java.util.function.Supplier;

public class TestParserFactory {

    private final MethodDeclarationProvider methodDeclarationProvider;
    private final Supplier<Kensa.Configuration> configurationSupplier;

    public TestParserFactory(Supplier<Kensa.Configuration> configurationSupplier, MethodDeclarationProvider methodDeclarationProvider) {
        this.configurationSupplier = configurationSupplier;
        this.methodDeclarationProvider = methodDeclarationProvider;
    }

    public TestParser create(TestInvocationContext context) {
        Kensa.Configuration configuration = configurationSupplier.get();

        return new TestParser(
                context,
                configuration.renderers(),
                methodDeclarationProvider,
                configuration.dictionary().keywordPattern(),
                configuration.dictionary().acronymPattern()
        );
    }
}
