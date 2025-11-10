package dev.kensa.runtime

import dev.kensa.context.NestedInvocationContextHolder
import dev.kensa.context.RenderedValueInvocationContextHolder
import dev.kensa.util.findMethod
import java.lang.reflect.Method
import kotlin.reflect.KClass

object CompilerPluginHookFunctions {

    fun onEnterNestedSentence(owner: Any?, fqName: String, simpleName: String, paramTypes: Array<KClass<*>>, args: Array<Any?>) {
        val method: Method = deriveMethod(owner, fqName, simpleName, paramTypes)

        NestedInvocationContextHolder.nestedSentenceInvocationContext().recordInvocation(method, args)
    }

    fun onExitRenderedValue(owner: Any?, fqName: String, simpleName: String, paramTypes: Array<KClass<*>>, returnValue: Any?) {
        val method: Method = deriveMethod(owner, fqName, simpleName, paramTypes)

        RenderedValueInvocationContextHolder.renderedValueInvocationContext().recordInvocation(method, returnValue)
    }

    private fun deriveMethod(owner: Any?, fqName: String, simpleName: String, paramTypes: Array<KClass<*>>): Method {
        val clazz = when (owner) {
            null -> Class.forName(fqName)
            else -> owner::class.java
        }

        return clazz.findMethod(simpleName)
    }
}