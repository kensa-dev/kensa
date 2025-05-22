package dev.kensa.context

import dev.kensa.fixture.Fixtures
import dev.kensa.state.TestMethodContainer
import dev.kensa.state.TestState
import dev.kensa.state.TestState.NotExecuted
import java.lang.reflect.Method
import java.util.UUID

class TestContainer(
    val testClass: Class<*>,
    val displayName: String,
    val methodContainers: Map<Method, TestMethodContainer>,
    val notes: String?,
    val issues: List<String>,
    val minimumUniquePackageName: String
) {

    val orderedMethodContainers: List<TestMethodContainer> by lazy { methodContainers.values.sortedBy { it.indexInSource } }

    val state: TestState
        get() = methodContainers.values
            .fold(NotExecuted) { state, invocationData ->
                state.overallStateFrom(invocationData.state)
            }

    fun <T> transform(transformer: (TestContainer) -> T): T = transformer(this)

    fun startTestInvocation(testInstance: Any, method: Method, arguments: List<Any?>, displayName: String, startTimeMs: Long, fixtures: Fixtures): UUID =
        methodContainers.getValue(method).startTestInvocation(testInstance, arguments, displayName, startTimeMs, fixtures)

    fun endTestInvocation(method: Method, testContext: TestContext, testId: UUID, executionException: Throwable?, endTimeMs: Long) {
        methodContainers.getValue(method).endTestInvocation(testContext, testId, executionException, endTimeMs)
    }
}