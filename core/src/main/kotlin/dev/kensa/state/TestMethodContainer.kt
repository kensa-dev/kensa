
package dev.kensa.state

import dev.kensa.Tab
import dev.kensa.context.OrgFlowSpec
import dev.kensa.context.TestContext
import dev.kensa.parse.ParseError
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.time.Duration.Companion.milliseconds

class TestMethodContainer internal constructor(private val testInvocationFactory: TestInvocationFactory, val method: Method, val displayName: String, val issues: List<String>, val epics: List<String>, val tags: List<String>, val orgFlow: OrgFlowSpec?, private val initialState: TestState, val autoOpenTab: Tab) {
    // Invocations of a single test method run concurrently when a parameterised test is executed in
    // parallel, and they all share this container, so both collections must tolerate concurrent writes.
    val invocationContexts: MutableMap<UUID, TestInvocationContext> = ConcurrentHashMap()
    private val _invocations = CopyOnWriteArrayList<TestInvocation>()
    val invocations: List<TestInvocation> get() = _invocations
    @Volatile
    private var _parseErrors: List<ParseError> = emptyList()
    internal val parseErrors: List<ParseError> get() = _parseErrors

    // TODO: Need a better way
    //    val indexInSource: Int by lazy { invocations.first().indexInSource }
    val indexInSource: Int by lazy { invocations.firstOrNull()?.indexInSource ?: 100 }

    fun startTestInvocation(testInstance: Any, arguments: List<Any?>, displayName: String, startTimeMs: Long, testContext: TestContext): UUID {
        val testId = UUID.randomUUID()

        invocationContexts[testId] = TestInvocationContext(
            testInstance,
            method,
            arguments.toTypedArray(),
            displayName,
            startTimeMs,
            testContext.fixtures,
            testContext.outputs
        )

        return testId
    }

    fun endTestInvocation(testContext: TestContext, testId: UUID, executionException: Throwable?, endTimeMs: Long) {
        val (invocation, parseErrors) = invocationContexts.getValue(testId).let { invocationContext ->
            testInvocationFactory.create(
                invocationContext.startTimeMs,
                (endTimeMs - invocationContext.startTimeMs).milliseconds,
                testContext,
                invocationContext,
                executionException,
                invocationContext.displayName
            )
        }
        _invocations.add(invocation)
        if (_parseErrors.isEmpty()) _parseErrors = parseErrors
    }

    val state: TestState
        get() = invocations.fold(initialState) { state, invocation ->
            state.overallStateFrom(invocation.state)
        }
}