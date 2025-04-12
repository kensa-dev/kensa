package dev.kensa.state

import dev.kensa.Tab
import dev.kensa.context.TestContext
import java.lang.reflect.Method
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

class TestMethodContainer(private val testInvocationFactory: TestInvocationFactory, val method: Method, val displayName: String, val notes: String?, val issues: List<String>, private val initialState: TestState, val autoOpenTab: Tab) {
    val invocationContexts = mutableMapOf<UUID, TestInvocationContext>()
    val invocations = mutableListOf<TestInvocation>()

    // TODO: Need a better way
    //    val indexInSource: Int by lazy { invocations.first().indexInSource }
    val indexInSource: Int by lazy { invocations.firstOrNull()?.indexInSource ?: 100 }

    fun startTestInvocation(testInstance: Any, arguments: List<Any?>, displayName: String, startTimeMs: Long): UUID {
        val testId = UUID.randomUUID()

        invocationContexts[testId] = TestInvocationContext(
            testInstance,
            method,
            arguments.toTypedArray(),
            displayName,
            startTimeMs
        )

        return testId
    }

    fun endTestInvocation(testContext: TestContext, testId: UUID, executionException: Throwable?, endTimeMs: Long) {
        invocations.add(
            invocationContexts.getValue(testId).let { invocationContext ->
                testInvocationFactory.create(
                    (endTimeMs - invocationContext.startTimeMs).milliseconds,
                    testContext,
                    invocationContext,
                    executionException,
                    invocationContext.displayName
                )
            }
        )
    }

    val state: TestState
        get() = invocations.fold(initialState) { state, invocation ->
            state.overallStateFrom(invocation.state)
        }
}