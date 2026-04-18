package dev.kensa.context

import dev.kensa.parse.ElementDescriptor
import dev.kensa.parse.ParsedExpandableMethod
import java.lang.reflect.Method

class ExpandableInvocationContext {

    private lateinit var methods: Map<String, ParsedExpandableMethod>

    private val invocations = mutableMapOf<String, MutableList<Array<Any?>>>()

    fun recordInvocation(method: Method, args: Array<Any?>) {
        invocations.compute(method.name) { _, current ->
            current?.also { it.add(args) } ?: mutableListOf(args)
        }
    }

    fun update(expandableMethods: Map<String, ParsedExpandableMethod>) {
        methods = expandableMethods
    }

    fun nextInvocationFor(name: String): ExpandableInvocation =
        if(invocations[name] == null)
            NoExpandableInvocation(Array(methods.getValue(name).parameters.descriptors.size) { name })
        else {
            RealExpandableInvocation(
                invocations.getValue(name).removeFirst(),
                methods.getValue(name).parameters.descriptors
            )
        }
}

sealed interface ExpandableInvocation {
    val arguments: Array<Any?>
    val parameters: Map<String, ElementDescriptor>
}

class NoExpandableInvocation(override val arguments: Array<Any?> = emptyArray()) : ExpandableInvocation {
    override val parameters: Map<String, ElementDescriptor> = emptyMap()
}

class RealExpandableInvocation(override val arguments: Array<Any?>, override val parameters: Map<String, ElementDescriptor>) : ExpandableInvocation