package dev.kensa.state;

import dev.kensa.Kensa;
import dev.kensa.context.TestContext;
import dev.kensa.parse.MethodDeclarationProvider;
import dev.kensa.parse.TestParserFactory;
import dev.kensa.render.diagram.SequenceDiagram;
import dev.kensa.render.diagram.SequenceDiagramFactory;

import java.time.Duration;
import java.util.function.Supplier;

import static dev.kensa.render.diagram.svg.SvgCompatiblePredicate.isSvgCompatible;

public class TestInvocationFactory {

    private final TestParserFactory testParserFactory;
    private final SequenceDiagramFactory sequenceDiagramFactory;
    private final Supplier<Kensa.Configuration> configuration;

    public TestInvocationFactory(Supplier<Kensa.Configuration> configuration) {
        this.configuration = configuration;
        this.sequenceDiagramFactory = new SequenceDiagramFactory(configuration);
        this.testParserFactory = new TestParserFactory(configuration, new MethodDeclarationProvider());
    }

    public TestInvocation create(Duration elapsedTime, TestContext testContext, TestInvocationContext testInvocationContext, Throwable throwable) {
        CapturedInteractions interactions = testContext.interactions();
        SequenceDiagram sequenceDiagram = null;
        if (interactions.containsEntriesMatching(isSvgCompatible())) {
            sequenceDiagram = sequenceDiagramFactory.create(testContext.interactions());
        }

        return new TestInvocation(
                elapsedTime,
                testParserFactory.create(testInvocationContext).parse(),
                testContext.givens(),
                testContext.interactions(),
                configuration.get().dictionary().acronyms(),
                throwable,
                sequenceDiagram
        );
    }
}
