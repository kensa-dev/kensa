package dev.kensa.junit

import dev.kensa.Kensa
import dev.kensa.KensaExecutionContext
import dev.kensa.context.*
import dev.kensa.output.TestWriter
import dev.kensa.output.ResultWriter
import dev.kensa.parse.TestInvocationParser
import dev.kensa.parse.java.JavaMethodParser
import dev.kensa.parse.kotlin.KotlinFunctionParser
import dev.kensa.render.diagram.SequenceDiagramFactory
import dev.kensa.state.TestInvocationContext
import dev.kensa.state.TestInvocationFactory
import org.junit.jupiter.api.extension.*
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource
import java.lang.reflect.Method
import java.time.Duration
import java.time.temporal.ChronoUnit

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
        println("KensaExtension.beforeAll::> ${context.uniqueId}")

        if (Kensa.configuration.isOutputEnabled) {
            with(context.getStore(KENSA)) {
                val executionContext = getOrCreateExecutionContext(context)
                val container = testContainerFactory.createFor(context.requiredTestClass, context.displayName)
                put(TEST_CONTAINER_KEY, CloseableContainer(container, TestWriter(Kensa.configuration)))
                executionContext.add(container)
            }
        }
    }

    override fun beforeEach(context: ExtensionContext) {
        println("KensaExtension.beforeEach::> ${context.uniqueId}")
        with(context.getStore(KENSA)) {
            TestContext(context.requiredTestClass, context.requiredTestMethod).also {
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
        println("KensaExtension.interceptTestTemplateMethod::> ${context.uniqueId}")
        createTestInvocationContext(context, invocationContext.arguments.toTypedArray())
        invocation.proceed()
    }

    // Called for @Test methods
    override fun interceptTestMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        context: ExtensionContext
    ) {
        println("KensaExtension.interceptTestMethod::> ${context.uniqueId}")
        createTestInvocationContext(context, invocationContext.arguments.toTypedArray())
        invocation.proceed()
    }

    private fun createTestInvocationContext(context: ExtensionContext, arguments: Array<Any?>) {
        if (Kensa.configuration.isOutputEnabled) {
            with(context.getStore(KENSA)) {
                put(
                    TEST_INVOCATION_CONTEXT_KEY, TestInvocationContext(
                        context.requiredTestInstance,
                        context.requiredTestMethod,
                        arguments,
                        System.currentTimeMillis()
                    )
                )
            }
        }
    }

    override fun afterTestExecution(context: ExtensionContext) {
        println("KensaExtension.afterTestExecution::> ${context.uniqueId}")
        try {
            if (Kensa.configuration.isOutputEnabled) {
                val endTime = System.currentTimeMillis()
                with(context.getStore(KENSA)) {
                    val testContext = get(TEST_CONTEXT_KEY, TestContext::class.java)
                    val testContainer = get(TEST_CONTAINER_KEY, CloseableContainer::class.java).container
                    val testInvocationContext = get(TEST_INVOCATION_CONTEXT_KEY, TestInvocationContext::class.java)

                    testContainer.testMethodContainerFor(context.requiredTestMethod)
                        .add(
                            testInvocationFactory.create(
                                Duration.of(endTime - testInvocationContext.startTime, ChronoUnit.MILLIS),
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
    private fun getOrCreateExecutionContext(context: ExtensionContext) =
        context.root.getStore(KENSA).getOrComputeIfAbsent(
            KENSA_EXECUTION_CONTEXT_KEY,
            EXECUTION_CONTEXT_FACTORY,
            CloseableContext::class.java
        ).context

    companion object {
        val KENSA: ExtensionContext.Namespace = ExtensionContext.Namespace.create("dev", "kensa")
        const val TEST_CONTEXT_KEY = "TestContext"
        private val EXECUTION_CONTEXT_FACTORY = { _: String ->
            CloseableContext(KensaExecutionContext(), ResultWriter(Kensa.configuration.outputDir, Kensa.configuration))
        }
        private const val TEST_CONTAINER_KEY = "TestContainer"
        private const val TEST_INVOCATION_CONTEXT_KEY = "TestArguments"
        private const val KENSA_EXECUTION_CONTEXT_KEY = "KensaExecutionContext"
    }
}

class CloseableContainer(val container: TestContainer, val testWriter: TestWriter) : CloseableResource {
    override fun close() {
        testWriter.write(container)
    }
}
class CloseableContext(val context: KensaExecutionContext, val resultWriter: ResultWriter) : CloseableResource {
    override fun close() {
        resultWriter.write(context.containers)
    }
}