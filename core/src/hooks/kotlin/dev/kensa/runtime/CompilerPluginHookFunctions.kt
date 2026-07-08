package dev.kensa.runtime

import dev.kensa.context.ExpandableInvocationContextHolder
import dev.kensa.context.RenderedValueInvocationContextHolder

object CompilerPluginHookFunctions {

    fun onEnterExpandableSentence(name: String, args: Array<Any?>) {
        ExpandableInvocationContextHolder.expandableSentenceInvocationContext().recordInvocation(name, args)
    }

    fun onExitRenderedValue(name: String, returnValue: Any?) {
        RenderedValueInvocationContextHolder.renderedValueInvocationContext().recordInvocation(name, returnValue)
    }
}
