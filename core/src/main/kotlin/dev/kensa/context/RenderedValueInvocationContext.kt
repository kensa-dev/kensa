package dev.kensa.context

import dev.kensa.util.normalisedPlatformName
import java.lang.reflect.Method

class RenderedValueInvocationContext {

    private val invocations = mutableMapOf<String, MutableList<Any?>>()

    fun recordInvocation(method: Method, returnValue: Any?) {
        recordInvocation(method.normalisedPlatformName, returnValue)
    }

    fun recordInvocation(name: String, returnValue: Any?) {
        invocations.compute(name) { _, current ->
            current?.also { it.add(returnValue) } ?: mutableListOf(returnValue)
        }
    }

    fun nextInvocationFor(name: String): RenderedValueInvocation {
        val list = invocations[name]
            ?.takeIf { it.isNotEmpty() }
            ?: return NoRenderedValueInvocation

        return RealRenderedValueInvocation(list.removeFirst())
    }
}

sealed interface RenderedValueInvocation {
    val returnValue: Any?
}

object NoRenderedValueInvocation : RenderedValueInvocation {
    override val returnValue: Any? = null
}

class RealRenderedValueInvocation(override val returnValue: Any?) : RenderedValueInvocation
