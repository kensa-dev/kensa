package dev.kensa.context

import java.lang.reflect.Method

class RenderedValueInvocationContext {

    private val invocations = mutableMapOf<Method, MutableList<Any?>>()

    fun recordInvocation(method: Method, returnValue: Any?) {
        invocations.compute(method) { key, current ->
            current?.also { it.add(returnValue) } ?: mutableListOf(returnValue)
        }
    }

    fun nextInvocationFor(name: String): RenderedValueInvocation =
        if (invocations.keys.find { it.name == name } == null)
            NoRenderedValueInvocation
        else {
            RealRenderedValueInvocation(invocations.keys.find { it.name == name }?.let { invocations.getValue(it).removeFirst() })
        }
}

sealed interface RenderedValueInvocation {
    val returnValue: Any?
}

object NoRenderedValueInvocation : RenderedValueInvocation {
    override val returnValue: Any? = null
}

class RealRenderedValueInvocation(override val returnValue: Any?) : RenderedValueInvocation
