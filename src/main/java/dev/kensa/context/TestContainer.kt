package dev.kensa.context

import dev.kensa.state.TestMethodInvocation
import dev.kensa.state.TestState
import dev.kensa.state.TestState.NotExecuted
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource
import java.lang.reflect.Method

class TestContainer(
    val testClass: Class<*>,
    val displayName: String,
    val invocations: Map<Method, TestMethodInvocation>,
    val notes: String?,
    val issues: List<String>,
    private val writeTestFile: (TestContainer) -> Unit
) : CloseableResource {
    val state: TestState
        get() = invocations.values
            .fold(NotExecuted) { state, invocationData ->
                state.overallStateFrom(invocationData.state)
            }

    fun testMethodInvocationFor(method: Method): TestMethodInvocation = invocations[method] ?: error("No method invocation found for test [${method.name}]")

    fun <T> transform(transformer: (TestContainer) -> T): T = transformer(this)

    override fun close() {
        writeTestFile(this)
    }
}