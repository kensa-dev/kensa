package dev.kensa.context

import dev.kensa.output.TestWriter
import dev.kensa.state.TestMethodContainer
import dev.kensa.state.TestState
import dev.kensa.state.TestState.NotExecuted
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource
import java.lang.reflect.Method

class TestContainer(
    val testClass: Class<*>,
    val classDisplayName: String,
    val methods: Map<Method, TestMethodContainer>,
    val notes: String?,
    val issues: List<String>,
    private val testWriter: TestWriter
) : CloseableResource {
    val state: TestState
        get() = methods.values
            .fold(NotExecuted) { state, invocationData ->
                state.overallStateFrom(invocationData.state)
            }

    fun testMethodContainerFor(method: Method): TestMethodContainer = methods[method] ?: error("No method invocation found for test [${method.name}]")

    fun <T> transform(transformer: (TestContainer) -> T): T = transformer(this)

    override fun close() {
        testWriter.write(this)
    }
}