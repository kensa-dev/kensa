package dev.kensa.context

import dev.kensa.state.TestMethodContainer
import dev.kensa.state.TestState
import dev.kensa.state.TestState.NotExecuted
import java.lang.reflect.Method

class TestContainer(
    val testClass: Class<*>,
    val classDisplayName: String,
    val methods: Map<Method, TestMethodContainer>,
    val notes: String?,
    val issues: List<String>
) {
    val state: TestState
        get() = methods.values
            .fold(NotExecuted) { state, invocationData ->
                state.overallStateFrom(invocationData.state)
            }

    fun testMethodContainerFor(method: Method): TestMethodContainer = methods[method] ?: error("No method invocation found for test [${method.name}]")

    fun <T> transform(transformer: (TestContainer) -> T): T = transformer(this)
}