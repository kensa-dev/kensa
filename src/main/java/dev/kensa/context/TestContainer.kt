package dev.kensa.context

import dev.kensa.state.TestMethodInvocation
import dev.kensa.state.TestState
import dev.kensa.state.TestState.NotExecuted
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

class TestContainer(
        val testClass: KClass<*>,
        val displayName: String,
        val invocations: Map<KFunction<*>, TestMethodInvocation>,
        val notes: String?,
        val issues: List<String>
) {
    val state: TestState
        get() = invocations.values
                .fold(NotExecuted) { state, invocationData ->
                    state.overallStateFrom(invocationData.state)
                }

    fun testMethodInvocationFor(kFunction: KFunction<*>): TestMethodInvocation = invocations[kFunction] ?: error("No method invocation found for test [${kFunction.name}]")

    fun <T> transform(transformer: (TestContainer) -> T): T = transformer(this)
}