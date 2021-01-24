package dev.kensa

import dev.kensa.context.TestContainer
import dev.kensa.context.TestContainerFactory
import dev.kensa.context.TestContext
import dev.kensa.context.TestContextHolder.bindToThread
import dev.kensa.context.TestContextHolder.clearFromThread
import dev.kensa.output.ResultWriter
import dev.kensa.parse.TestInvocationParser
import dev.kensa.parse.java.JavaMethodParser
import dev.kensa.parse.kotlin.KotlinFunctionParser
import dev.kensa.render.diagram.SequenceDiagramFactory
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens
import dev.kensa.state.TestInvocationContext
import dev.kensa.state.TestInvocationFactory
import org.junit.jupiter.api.extension.*
import java.lang.reflect.Method
import java.time.Duration
import java.time.temporal.ChronoUnit.MILLIS

class KensaExtension : Extension, BeforeAllCallback, BeforeEachCallback,
    AfterTestExecutionCallback, InvocationInterceptor {
    private val testContainerFactory = TestContainerFactory()
    private val testInvocationFactory = TestInvocationFactory(
        TestInvocationParser(),
        JavaMethodParser(),
        KotlinFunctionParser(),
        SequenceDiagramFactory
    )

    override fun beforeAll(context: ExtensionContext) {
        with(context.getStore(KENSA)) {
            val executionContext = bindToRootContextOf(context)
            val container = testContainerFactory.createFor(context)
            put(TEST_CONTAINER_KEY, container)
            executionContext.register(container)
        }
    }

    override fun beforeEach(context: ExtensionContext) {
        with(context.getStore(KENSA)) {
            TestContext(Givens(), CapturedInteractions()).also {
                put(TEST_CONTEXT_KEY, it)
                bindToThread(it)
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

    override fun afterTestExecution(context: ExtensionContext) {
        try {
            val endTime = System.currentTimeMillis()
            with(context.getStore(KENSA)) {
                val startTime = get(TEST_START_TIME_KEY, Long::class.java)
                val testContext = get(TEST_CONTEXT_KEY, TestContext::class.java)
                val testContainer = get(TEST_CONTAINER_KEY, TestContainer::class.java)
                val invocation = testContainer.testMethodInvocationFor(context.requiredTestMethod!!)
                val testInvocationContext = get(TEST_INVOCATION_CONTEXT_KEY, TestInvocationContext::class.java)
                invocation.add(
                    testInvocationFactory.create(
                        Duration.of(endTime - startTime, MILLIS),
                        testContext,
                        testInvocationContext,
                        context.executionException.orElse(null)
                    )
                )
            }
        } finally {
            clearFromThread()
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
        private val EXECUTION_CONTEXT_FACTORY = { _: String -> KensaExecutionContext(ResultWriter()) }
        private const val TEST_START_TIME_KEY = "StartTime"
        private const val TEST_CONTAINER_KEY = "TestContainer"
        private const val TEST_INVOCATION_CONTEXT_KEY = "TestArguments"
        private const val KENSA_EXECUTION_CONTEXT_KEY = "KensaExecutionContext"
    }
}