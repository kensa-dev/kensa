package dev.kensa.context

import dev.kensa.parse.ElementDescriptor
import dev.kensa.parse.ParsedNestedMethod
import java.lang.reflect.Method

class NestedInvocationContext {

    private lateinit var nestedMethods: Map<String, ParsedNestedMethod>

    private val invocations = mutableMapOf<String, MutableList<Array<Any?>>>()

    fun recordInvocation(method: Method, args: Array<Any?>) {
        invocations.compute(method.name) { key, current ->
            current?.also { it.add(args) } ?: mutableListOf(args)
        }
    }

    fun update(nestedSentences: Map<String, ParsedNestedMethod>) {
        nestedMethods = nestedSentences
    }

    fun nextInvocationFor(name: String): NestedInvocation =
        if(invocations[name] == null)
            NoInvocation(Array(nestedMethods.getValue(name).parameters.descriptors.size) { name })
        else {
            RealNestedInvocation(
                invocations.getValue(name).removeFirst(),
                nestedMethods.getValue(name).parameters.descriptors
            )
        }
}

sealed interface NestedInvocation {
    val arguments: Array<Any?>
    val parameters: Map<String, ElementDescriptor>
}

class NoInvocation(
    override val arguments: Array<Any?> = emptyArray()
) : NestedInvocation {
    override val parameters: Map<String, ElementDescriptor> = emptyMap()
}

class RealNestedInvocation(override val arguments: Array<Any?>, override val parameters: Map<String, ElementDescriptor>) : NestedInvocation