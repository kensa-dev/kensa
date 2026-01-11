package dev.kensa.junit

import dev.kensa.Configuration
import dev.kensa.KensaConfigurationProvider
import dev.kensa.StaticKensaConfigurationProvider
import dev.kensa.context.*
import dev.kensa.output.ResultWriter
import dev.kensa.parse.CompositeParserDelegate
import dev.kensa.parse.MethodParser
import dev.kensa.parse.ParserCache
import dev.kensa.parse.TestInvocationParser
import dev.kensa.parse.java.JavaParserDelegate
import dev.kensa.parse.kotlin.KotlinParserDelegate
import dev.kensa.render.diagram.SequenceDiagramFactory
import dev.kensa.state.TestInvocationFactory
import dev.kensa.state.TestState.Disabled
import dev.kensa.state.TestState.NotExecuted
import dev.kensa.util.findAnnotation
import dev.kensa.util.findTestMethods
import dev.kensa.util.hasAnnotation
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.*
import org.junit.jupiter.params.ParameterizedTest
import java.io.Closeable
import java.lang.reflect.Method
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubclassOf

class KensaExtension : Extension, BeforeAllCallback, BeforeEachCallback, AfterTestExecutionCallback, InvocationInterceptor {

    override fun beforeAll(context: ExtensionContext) {
        if (context.kensaConfiguration.isOutputEnabled) {
            with(context) {
                val testContainer = kensaContext.createTestContainer(requiredTestClass, displayName, TestPlanDetails.commonBasePackage)
                kensaStore.put(TEST_CONTAINER_KEY, CloseableTestContainer(context.kensaResultWriter, testContainer))
            }
        }
    }

    override fun beforeEach(context: ExtensionContext) {
        with(context) {
            TestContext(requiredTestClass, requiredTestMethod, context.kensaConfiguration.setupStrategy).also {
                TestContextHolder.bindToCurrentThread(it)
                kensaStore.put(TEST_CONTEXT_KEY, it)
            }
            NestedInvocationContextHolder.bindToCurrentThread(NestedInvocationContext())
            RenderedValueInvocationContextHolder.bindToCurrentThread(RenderedValueInvocationContext())
        }
    }

    // Called for @ParameterizedTest methods
    override fun interceptTestTemplateMethod(invocation: InvocationInterceptor.Invocation<Void>, invocationContext: ReflectiveInvocationContext<Method>, context: ExtensionContext) {
        startTestInvocation(context, invocationContext.arguments)
        invocation.proceed()
    }

    // Called for @Test methods
    override fun interceptTestMethod(invocation: InvocationInterceptor.Invocation<Void>, invocationContext: ReflectiveInvocationContext<Method>, context: ExtensionContext) {
        startTestInvocation(context, invocationContext.arguments)
        invocation.proceed()
    }

    private fun startTestInvocation(context: ExtensionContext, arguments: List<Any?>) {
        if (context.kensaConfiguration.isOutputEnabled) {
            with(context) {
                kensaStore.put(
                    TEST_INVOCATION_KEY,
                    kensaContext.startTestInvocation(
                        requiredTestInstance,
                        requiredTestClass,
                        requiredTestMethod,
                        arguments,
                        context.displayName,
                        System.currentTimeMillis(),
                        TestContextHolder.testContext(),
                    )
                )
            }
        }
    }

    override fun afterTestExecution(context: ExtensionContext) {
        try {
            if (context.kensaConfiguration.isOutputEnabled) {
                with(context) {
                    kensaContext.endTestInvocation(
                        requiredTestClass,
                        requiredTestMethod,
                        TestContextHolder.testContext(),
                        kensaStore.get(TEST_INVOCATION_KEY, UUID::class.java),
                        context.executionException.orElse(null),
                        System.currentTimeMillis()
                    )
                }
            }
        } finally {
            TestContextHolder.clearFromThread()
            NestedInvocationContextHolder.clearFromThread()
            RenderedValueInvocationContextHolder.clearFromThread()
        }
    }

    companion object {

        private val ExtensionContext.kensaStore get() = getStore(kensaNamespace)

        private val configurationProviderClassName: String? = System.getProperty(CONFIGURATION_PROVIDER_PROPERTY)

        private val configuration: KensaConfigurationProvider =
            configurationProviderClassName?.let { loadConfigurationProvider(it) } ?: StaticKensaConfigurationProvider

        @Suppress("UNCHECKED_CAST")
        private fun loadConfigurationProvider(className: String): KensaConfigurationProvider = className.let {
            val klass = Class.forName(className).kotlin

            if (!klass.isSubclassOf(KensaConfigurationProvider::class)) {
                throw IllegalArgumentException("The 'dev.kensa.ConfigurationProvider' property must point to a '${KensaConfigurationProvider::class.qualifiedName}'")
            }

            with(klass as KClass<KensaConfigurationProvider>) {
                objectInstance ?: createInstance()
            }
        }

        @get:Synchronized
        private val ExtensionContext.kensaContext
            get() = root.kensaStore.getOrComputeIfAbsent(
                KENSA_CONTEXT_KEY,
                kensaContextFactory(kensaResultWriter, kensaConfiguration, ParserCache()),
                CloseableKensaContext::class.java
            ).context

        @get:Synchronized
        private val ExtensionContext.kensaConfiguration
            get() = root.kensaStore.getOrComputeIfAbsent(
                KENSA_CONFIGURATION_KEY,
                { configuration() },
                Configuration::class.java
            )

        @get:Synchronized
        private val ExtensionContext.kensaResultWriter
            get() = root.kensaStore.getOrComputeIfAbsent(
                KENSA_RESULT_WRITER_KEY,
                { ResultWriter(this@kensaResultWriter.kensaConfiguration) },
                ResultWriter::class.java
            )

        private fun testInvocationFactory(configuration: Configuration, parserCache: ParserCache): TestInvocationFactory {
            return TestInvocationFactory(
                TestInvocationParser(configuration),
                MethodParser(
                    parserCache,
                    configuration,
                    CompositeParserDelegate(
                        configuration.sourceCode,
                        listOf(
                            JavaParserDelegate(isJavaClassTest, isJavaInterfaceTest, configuration.antlrErrorListenerDisabled, configuration.antlrPredicationMode, configuration.sourceCode),
                            KotlinParserDelegate(isKotlinTest, configuration.antlrErrorListenerDisabled, configuration.antlrPredicationMode, configuration.sourceCode),
                        )
                    )
                ),
                SequenceDiagramFactory(configuration.umlDirectives)
            )
        }

        private fun kensaContextFactory(resultWriter: ResultWriter, configuration: Configuration, parserCache: ParserCache) = { _: String ->
            CloseableKensaContext(
                resultWriter,
                KensaContext(
                    TestContainerFactory(
                        initialStateFor = { md -> if (md.hasAnnotation<Disabled>()) Disabled else NotExecuted },
                        displayNameFor = { md -> md.findAnnotation<DisplayName>()?.value },
                        findTestMethods = { cs -> cs.findTestMethods { it.hasAnnotation<Test>() || it.hasAnnotation<ParameterizedTest>() } },
                        testInvocationFactory(configuration, parserCache),
                        configuration
                    )
                )
            )
        }

        const val CONFIGURATION_PROVIDER_PROPERTY = "dev.kensa.ConfigurationProvider"

        private val kensaNamespace: ExtensionContext.Namespace = ExtensionContext.Namespace.create("dev", "kensa")

        private const val KENSA_CONTEXT_KEY = "KensaContext"
        private const val KENSA_CONFIGURATION_KEY = "KensaConfiguration"
        private const val TEST_CONTEXT_KEY = "TestContext"
        private const val TEST_CONTAINER_KEY = "TestContainer"
        private const val TEST_INVOCATION_KEY = "TestArguments"
        private const val KENSA_RESULT_WRITER_KEY = "KensaResultWriter"
    }
}

class CloseableTestContainer(private val resultWriter: ResultWriter, val container: TestContainer) : Closeable {
    override fun close() {
        resultWriter.writeTest(container)
    }
}

class CloseableKensaContext(private val resultWriter: ResultWriter, val context: KensaContext) : Closeable {
    override fun close() {
        resultWriter.write(context.testContainers)
    }
}