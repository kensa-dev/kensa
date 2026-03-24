package dev.kensa.junit

import dev.kensa.context.KensaLifecycleManager
import dev.kensa.context.TestContainer
import org.junit.jupiter.api.extension.*
import java.io.Closeable
import java.lang.reflect.Method
import java.util.*

class KensaExtension : Extension, BeforeAllCallback, BeforeEachCallback, AfterTestExecutionCallback, InvocationInterceptor {

    override fun beforeAll(context: ExtensionContext) {
        val manager = context.manager
        manager.beforeClass(context.requiredTestClass, context.displayName)?.let { container ->
            context.kensaStore.put(TEST_CONTAINER_KEY, CloseableTestContainer(manager, container))
        }
    }

    override fun beforeEach(context: ExtensionContext) {
        context.manager.beforeTest(context.requiredTestClass, context.requiredTestMethod)
    }

    override fun interceptTestMethod(invocation: InvocationInterceptor.Invocation<Void?>, invocationContext: ReflectiveInvocationContext<Method>, context: ExtensionContext) {
        startTestInvocation(context, invocationContext.arguments)
        invocation.proceed()
    }

    override fun interceptTestTemplateMethod(invocation: InvocationInterceptor.Invocation<Void?>, invocationContext: ReflectiveInvocationContext<Method>, context: ExtensionContext) {
        startTestInvocation(context, invocationContext.arguments)
        invocation.proceed()
    }

    private fun startTestInvocation(context: ExtensionContext, arguments: List<Any?>) {
        context.kensaStore.put(
            TEST_INVOCATION_KEY,
            context.manager.startInvocation(
                context.requiredTestInstance,
                context.requiredTestClass,
                context.requiredTestMethod,
                arguments,
                context.displayName,
            )
        )
    }

    override fun afterTestExecution(context: ExtensionContext) {
        context.manager.endInvocation(
            context.requiredTestClass,
            context.requiredTestMethod,
            context.kensaStore.get(TEST_INVOCATION_KEY, UUID::class.java),
            context.executionException.orElse(null),
        )
    }

    companion object {
        const val CONFIGURATION_PROVIDER_PROPERTY = KensaLifecycleManager.CONFIGURATION_PROVIDER_PROPERTY

        private val kensaNamespace: ExtensionContext.Namespace = ExtensionContext.Namespace.create("dev", "kensa")

        private val ExtensionContext.kensaStore get() = getStore(kensaNamespace)

        @get:Synchronized
        private val ExtensionContext.manager: KensaLifecycleManager
            get() = root.kensaStore.computeIfAbsent(
                MANAGER_KEY,
                { _ -> KensaLifecycleManager.current() ?: KensaLifecycleManager.initialise(junit6Descriptor) },
                KensaLifecycleManager::class.java
            )

        private const val MANAGER_KEY = "KensaLifecycleManager"
        private const val TEST_CONTAINER_KEY = "TestContainer"
        private const val TEST_INVOCATION_KEY = "TestInvocation"
    }
}

private class CloseableTestContainer(private val manager: KensaLifecycleManager, val container: TestContainer) : Closeable {
    override fun close() {
        manager.writeTestResult(container)
    }
}
