package dev.kensa.runtime

import dev.kensa.context.ExpandableInvocationContextHolder
import dev.kensa.context.RenderedValueInvocationContextHolder
import dev.kensa.util.allMethods
import dev.kensa.util.findMethod
import dev.kensa.util.normalisedPlatformName
import dev.kensa.util.originalParamTypes
import java.lang.reflect.Method
import kotlin.reflect.KClass

object CompilerPluginHookFunctions {

    fun onEnterExpandableSentence(owner: Any?, fqName: String, simpleName: String, paramTypes: Array<KClass<*>>, args: Array<Any?>) {
        val method: Method = deriveMethod(owner, fqName, simpleName, paramTypes)

        ExpandableInvocationContextHolder.expandableSentenceInvocationContext().recordInvocation(method, args)
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

        return clazz
            .allMethods
            .firstOrNull { it.normalisedPlatformName == simpleName && it.originalParamTypes contentEquals paramTypes }
            // Fallback to the original findMethod call, which doesn't know about methods with "value class" parameters
            ?: clazz.findMethod(simpleName, *paramTypes.map { it.java }.toTypedArray())
    }
}