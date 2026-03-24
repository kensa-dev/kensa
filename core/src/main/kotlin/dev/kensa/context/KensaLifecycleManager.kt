package dev.kensa.context

import dev.kensa.Configuration
import dev.kensa.KensaConfigurationProvider
import dev.kensa.StaticKensaConfigurationProvider
import dev.kensa.output.ResultWriter
import dev.kensa.parse.CompositeParserDelegate
import dev.kensa.parse.MethodParser
import dev.kensa.parse.ParserCache
import dev.kensa.parse.TestInvocationParser
import dev.kensa.parse.java.JavaParserDelegate
import dev.kensa.parse.kotlin.KotlinParserDelegate
import dev.kensa.render.diagram.SequenceDiagramFactory
import dev.kensa.state.TestInvocationFactory
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubclassOf

class KensaLifecycleManager private constructor(
    val configuration: Configuration,
    private val kensaContext: KensaContext,
    private val resultWriter: Lazy<ResultWriter>,
) {


    val isOutputEnabled: Boolean get() = configuration.isOutputEnabled

    fun beforeClass(testClass: Class<*>, displayName: String): TestContainer? {
        if (!isOutputEnabled) return null
        return kensaContext.createTestContainer(testClass, displayName)
    }

    fun beforeTest(testClass: Class<*>, testMethod: Method) {
        TestContext(testClass, testMethod, configuration.setupStrategy).also {
            TestContextHolder.bindToCurrentThread(it)
        }
        NestedInvocationContextHolder.bindToCurrentThread(NestedInvocationContext())
        RenderedValueInvocationContextHolder.bindToCurrentThread(RenderedValueInvocationContext())
    }

    fun startInvocation(instance: Any, testClass: Class<*>, testMethod: Method, arguments: List<Any?>, displayName: String): UUID? {
        if (!isOutputEnabled) return null
        return kensaContext.startTestInvocation(
            instance,
            testClass,
            testMethod,
            arguments,
            displayName,
            System.currentTimeMillis(),
            TestContextHolder.testContext(),
        )
    }

    fun endInvocation(testClass: Class<*>, testMethod: Method, invocationId: UUID?, exception: Throwable?) {
        try {
            if (isOutputEnabled && invocationId != null) {
                kensaContext.endTestInvocation(
                    testClass,
                    testMethod,
                    TestContextHolder.testContext(),
                    invocationId,
                    exception,
                    System.currentTimeMillis()
                )
            }
        } finally {
            TestContextHolder.clearFromThread()
            NestedInvocationContextHolder.clearFromThread()
            RenderedValueInvocationContextHolder.clearFromThread()
        }
    }

    fun skipClass(testClass: Class<*>, displayName: String) {
        if (!isOutputEnabled) return
        val container = kensaContext.createTestContainer(testClass, displayName)
        resultWriter.value.writeTest(container)
    }

    fun writeTestResult(container: TestContainer) {
        if (!isOutputEnabled) return
        resultWriter.value.writeTest(container)
    }

    fun writeAllResults() {
        if (!isOutputEnabled) return
        resultWriter.value.write(kensaContext.testContainers)
    }

    companion object {

        const val CONFIGURATION_PROVIDER_PROPERTY = "dev.kensa.ConfigurationProvider"

        private val currentInstance = AtomicReference<KensaLifecycleManager?>(null)

        fun current(): KensaLifecycleManager? = currentInstance.get()

        fun initialise(descriptor: FrameworkDescriptor): KensaLifecycleManager {
            val configuration = loadConfiguration()
            val parserCache = ParserCache()
            val testInvocationFactory = testInvocationFactory(configuration, parserCache, descriptor)
            val testContainerFactory = TestContainerFactory(
                descriptor.initialStateFor,
                descriptor.displayNameFor,
                descriptor.findTestMethods,
                testInvocationFactory,
                configuration
            )
            val kensaContext = KensaContext(testContainerFactory)
            val resultWriter = lazy { ResultWriter(configuration) }
            return KensaLifecycleManager(configuration, kensaContext, resultWriter)
                .also { currentInstance.set(it) }
        }

        private fun loadConfiguration(): Configuration {
            val providerClassName: String? = System.getProperty(CONFIGURATION_PROVIDER_PROPERTY)
            val provider: KensaConfigurationProvider = providerClassName?.let { loadConfigurationProvider(it) } ?: StaticKensaConfigurationProvider
            return provider()
        }

        @Suppress("UNCHECKED_CAST")
        private fun loadConfigurationProvider(className: String): KensaConfigurationProvider {
            val klass = Class.forName(className).kotlin
            if (!klass.isSubclassOf(KensaConfigurationProvider::class)) {
                throw IllegalArgumentException("The 'dev.kensa.ConfigurationProvider' property must point to a '${KensaConfigurationProvider::class.qualifiedName}'")
            }
            return with(klass as KClass<KensaConfigurationProvider>) {
                objectInstance ?: createInstance()
            }
        }

        private fun testInvocationFactory(configuration: Configuration, parserCache: ParserCache, descriptor: FrameworkDescriptor): TestInvocationFactory =
            TestInvocationFactory(
                TestInvocationParser(configuration),
                MethodParser(
                    parserCache,
                    configuration,
                    CompositeParserDelegate(
                        configuration.sourceCode,
                        listOf(
                            JavaParserDelegate(descriptor.isJavaClassTest, descriptor.isJavaInterfaceTest, configuration.antlrErrorListenerDisabled, configuration.antlrPredicationMode, configuration.sourceCode),
                            KotlinParserDelegate(descriptor.isKotlinTest, configuration.antlrErrorListenerDisabled, configuration.antlrPredicationMode, configuration.sourceCode),
                        )
                    )
                ),
                SequenceDiagramFactory(configuration.umlDirectives)
            )
    }
}
