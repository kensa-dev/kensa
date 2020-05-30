package dev.kensa.state

import java.util.*
import kotlin.reflect.KFunction

class TestMethodInvocation(val testFunction: KFunction<*>, val displayName: String, val notes: String?, val issues: List<String>, private val initialState: TestState) {
    val invocations: MutableList<TestInvocation> = ArrayList()

    fun add(invocation: TestInvocation) {
        invocations.add(invocation)
    }

    val state: TestState
        get() = invocations.fold(initialState) { state, invocation ->
            state.overallStateFrom(invocation.state)
        }
}