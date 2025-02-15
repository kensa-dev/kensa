package dev.kensa.state

import dev.kensa.Tab
import java.lang.reflect.Method
import java.util.*

class TestMethodContainer(val method: Method, val displayName: String, val notes: String?, val issues: List<String>, private val initialState: TestState, val autoOpenTab: Tab) {
    val invocations: MutableList<TestInvocation> = ArrayList()

    fun add(invocation: TestInvocation) {
        invocations.add(invocation)
    }

    val state: TestState
        get() = invocations.fold(initialState) { state, invocation ->
            state.overallStateFrom(invocation.state)
        }
}