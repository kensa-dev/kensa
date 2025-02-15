package dev.kensa.junit

import dev.kensa.Kensa
import dev.kensa.KensaExecutionContext
import dev.kensa.context.*
import dev.kensa.output.ResultWriter
import dev.kensa.parse.TestInvocationParser
import dev.kensa.parse.java.JavaMethodParser
import dev.kensa.output.DefaultIndexWriter
import dev.kensa.output.DefaultTestWriter
import dev.kensa.parse.kotlin.KotlinFunctionParser
import dev.kensa.render.diagram.SequenceDiagramFactory
import dev.kensa.state.TestInvocationContext
import dev.kensa.state.TestInvocationFactory
import org.junit.jupiter.api.extension.*
import java.lang.reflect.Method
import java.time.Duration
import java.time.temporal.ChronoUnit

class KensaExtension : Extension, BeforeAllCallback, BeforeEachCallback,
    AfterTestExecutionCallback, InvocationInterceptor {
    private val testContextFactory = TestContextFactory()
    private val testContainerFactory = TestContainerFactory()
    private val testInvocationFactory = TestInvocationFactory(
        TestInvocationParser(),
        JavaMethodParser(),
        KotlinFunctionParser(),
        SequenceDiagramFactory
    )

    override fun beforeAll(context: ExtensionContext) {
        if (Kensa.configuration.isOutputEnabled) {
            with(context.getStore(KENSA)) {
                val executionContext = bindToRootContextOf(context)
                val container = testContainerFactory.createFor(context, DefaultTestWriter(Kensa.configuration))
                put(TEST_CONTAINER_KEY, container)
                executionContext.register(container)
            }
        }
    }

    override fun beforeEach(context: ExtensionContext) {
        with(context.getStore(KENSA)) {
            testContextFactory.createFor(context).also {
                put(TEST_CONTEXT_KEY, it)
                TestContextHolder.bindToThread(it)
            }
        }
    }

    // Called for @ParameterizedTest methods
    override fun interceptTestTemplateMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        context: ExtensionContext
    ) {
        createTestInvocationContext(context, invocationContext.arguments.toTypedArray())
        invocation.proceed()
    }

    // Called for @Test methods
    override fun interceptTestMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        context: ExtensionContext
    ) {
        createTestInvocationContext(context, invocationContext.arguments.toTypedArray())
        invocation.proceed()
    }

    private fun createTestInvocationContext(context: ExtensionContext, arguments: Array<Any?>) {
        if (Kensa.configuration.isOutputEnabled) {
            with(context.getStore(KENSA)) {
                put(TEST_START_TIME_KEY, System.currentTimeMillis())
                put(
                    TEST_INVOCATION_CONTEXT_KEY, TestInvocationContext(
                        context.requiredTestInstance,
                        context.requiredTestMethod,
                        arguments
                    )
                )
            }
        }
    }

    override fun afterTestExecution(context: ExtensionContext) {
        try {
            if (Kensa.configuration.isOutputEnabled) {
                val endTime = System.currentTimeMillis()
                with(context.getStore(KENSA)) {
                    val startTime = get(TEST_START_TIME_KEY, Long::class.java)
                    val testContext = get(TEST_CONTEXT_KEY, TestContext::class.java)
                    val testContainer = get(TEST_CONTAINER_KEY, TestContainer::class.java)
                    val testInvocationContext = get(TEST_INVOCATION_CONTEXT_KEY, TestInvocationContext::class.java)
                    testContainer.testMethodContainerFor(context.requiredTestMethod)
                        .add(
                            testInvocationFactory.create(
                                Duration.of(endTime - startTime, ChronoUnit.MILLIS),
                                testContext,
                                testInvocationContext,
                                context.executionException.orElse(null),
                                context.displayName
                            )
                        )
                }
            }
        } finally {
            TestContextHolder.clearFromThread()
        }
    }

    // Add the KensaExecutionContext to the store so we can hook up the close method to be executed when the
    // whole test run is complete
    @Synchronized
    private fun bindToRootContextOf(context: ExtensionContext) =
        context.root.getStore(KENSA).getOrComputeIfAbsent(
            KENSA_EXECUTION_CONTEXT_KEY,
            EXECUTION_CONTEXT_FACTORY,
            KensaExecutionContext::class.java
        )

    companion object {
        val KENSA: ExtensionContext.Namespace = ExtensionContext.Namespace.create("dev", "kensa")
        const val TEST_CONTEXT_KEY = "TestContext"
        private val EXECUTION_CONTEXT_FACTORY = { _: String ->
            KensaExecutionContext(ResultWriter(Kensa.configuration.outputDir, DefaultIndexWriter(Kensa.configuration)))
        }
        private const val TEST_START_TIME_KEY = "StartTime"
        private const val TEST_CONTAINER_KEY = "TestContainer"
        private const val TEST_INVOCATION_CONTEXT_KEY = "TestArguments"
        private const val KENSA_EXECUTION_CONTEXT_KEY = "KensaExecutionContext"
    }
}