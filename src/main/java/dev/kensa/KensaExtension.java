package dev.kensa;

import dev.kensa.context.TestContainer;
import dev.kensa.context.TestContainerFactory;
import dev.kensa.context.TestContext;
import dev.kensa.output.ResultWriter;
import dev.kensa.parse.MethodDeclarationProvider;
import dev.kensa.parse.ParsedTest;
import dev.kensa.parse.TestParser;
import dev.kensa.render.diagram.SequenceDiagramFactory;
import dev.kensa.sentence.Dictionary;
import dev.kensa.state.CapturedInteractions;
import dev.kensa.state.Givens;
import dev.kensa.state.TestInvocation;
import dev.kensa.state.TestInvocationData;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.function.Function;

import static dev.kensa.context.TestContextHolder.bindToThread;
import static dev.kensa.context.TestContextHolder.clearFromThread;
import static dev.kensa.util.ReflectionUtil.fieldValue;
import static dev.kensa.util.ReflectionUtil.invokeMethod;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.stream.Collectors.toList;

public class KensaExtension implements Extension, BeforeAllCallback, AfterTestExecutionCallback, BeforeTestExecutionCallback {

    private static final ExtensionContext.Namespace KENSA = ExtensionContext.Namespace.create(new Object());
    private static final String TEST_START_TIME_KEY = "StartTime";
    private static final String TEST_CONTAINER_KEY = "TestContainer";
    private static final String TEST_CONTEXT_KEY = "TestContext";
    private static final String TEST_PARSER_KEY = "TestParser";
    private static final String TEST_GIVENS_KEY = "TestGivens";
    private static final String TEST_INTERACTIONS_KEY = "TestInteractions";
    private static final String KENSA_EXECUTION_CONTEXT_KEY = "KensaExecutionContext";

    private static final Function<String, KensaExecutionContext> EXECUTION_CONTEXT_FACTORY =
            key -> new KensaExecutionContext(new ResultWriter(Kensa.configuration()));
    private static final MethodDeclarationProvider METHOD_DECLARATION_PROVIDER = new MethodDeclarationProvider();

    private final TestContainerFactory testContainerFactory = new TestContainerFactory();
    private final SequenceDiagramFactory sequenceDiagramFactory = new SequenceDiagramFactory(Kensa.configuration().umlDirectives());

    @Override
    public void beforeAll(ExtensionContext context) {
        KensaExecutionContext executionContext = bindToRootContextOf(context);

        ExtensionContext.Store store = context.getStore(KENSA);
        TestContainer container = testContainerFactory.createFor(context);

        store.put(TEST_CONTAINER_KEY, container);
        executionContext.register(container);
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        ExtensionContext.Store store = context.getStore(KENSA);
        store.put(TEST_START_TIME_KEY, System.currentTimeMillis());
        Givens givens = new Givens();
        CapturedInteractions interactions = new CapturedInteractions();
        TestContext testContext = new TestContext(givens, interactions);
        store.put(TEST_GIVENS_KEY, givens);
        store.put(TEST_INTERACTIONS_KEY, interactions);
        store.put(TEST_CONTEXT_KEY, testContext);
        bindToThread(testContext);

        // Workaround for JUnit5 argument access
        processTestMethodArguments(context, argumentsFrom(context));
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        try {
            long endTime = System.currentTimeMillis();

            ExtensionContext.Store store = context.getStore(KENSA);
            Long startTime = store.get(TEST_START_TIME_KEY, Long.class);

            TestContainer testContainer = store.get(TEST_CONTAINER_KEY, TestContainer.class);
            TestInvocationData invocationData = testContainer.invocationDataFor(context.getRequiredTestMethod());
            CapturedInteractions interactions = store.get(TEST_INTERACTIONS_KEY, CapturedInteractions.class);
            TestParser testParser = store.get(TEST_PARSER_KEY, TestParser.class);
            ParsedTest parsedTest = testParser.parse();

            invocationData.add(
                    new TestInvocation(
                            Duration.of(endTime - startTime, MILLIS),
                            parsedTest,
                            store.get(TEST_GIVENS_KEY, Givens.class),
                            interactions,
                            new ArrayList<>(),
                            Dictionary.acronyms().collect(toList()),
                            context.getExecutionException().orElse(null),
                            sequenceDiagramFactory.create(interactions)
                    )
            );
        } finally {
            clearFromThread();
        }
    }

    // TODO:: Need to watch this issue and remove reflection once extension point added in JUnit5
    // TODO:: https://github.com/junit-team/junit5/issues/1139
    @SuppressWarnings("WeakerAccess")
    public void processTestMethodArguments(ExtensionContext context, Object[] arguments) {
        ExtensionContext.Store store = context.getStore(KENSA);
        store.put(TEST_PARSER_KEY, new TestParser(context.getRequiredTestInstance(), context.getRequiredTestMethod(), arguments, METHOD_DECLARATION_PROVIDER));
    }

    private Object[] argumentsFrom(ExtensionContext context) {
        try {
            TestMethodTestDescriptor testDescriptor = invokeMethod(context, "getTestDescriptor", TestMethodTestDescriptor.class);
            TestTemplateInvocationContext invocationContext = fieldValue(testDescriptor, "invocationContext", TestTemplateInvocationContext.class);
            return fieldValue(invocationContext, "arguments", Object[].class);
        } catch (Exception e) {
            return new Object[0];
        }
    }

    // Add the KensaExecutionContext to the store so we can hook up the close method to be executed when the
    // whole test run is complete
    private synchronized KensaExecutionContext bindToRootContextOf(ExtensionContext context) {
        ExtensionContext.Store store = context.getRoot().getStore(KENSA);

        return store.getOrComputeIfAbsent(KENSA_EXECUTION_CONTEXT_KEY, EXECUTION_CONTEXT_FACTORY, KensaExecutionContext.class);
    }
}