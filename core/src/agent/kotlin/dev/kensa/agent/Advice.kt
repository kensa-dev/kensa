package dev.kensa.agent

import dev.kensa.context.NestedInvocationContextHolder
import dev.kensa.context.RenderedValueInvocationContextHolder
import net.bytebuddy.asm.Advice
import java.lang.reflect.Method

object NestedSentenceAdvice {
    @JvmStatic
    @Advice.OnMethodEnter
    fun enter(
        @Advice.Origin method: Method,
        @Advice.AllArguments args: Array<Any?>
    ) {
        NestedInvocationContextHolder.nestedSentenceInvocationContext().recordInvocation(method, args)
    }
}

object RenderedValueAdvice {
    @JvmStatic
    @Advice.OnMethodExit
    fun exit(
        @Advice.Origin method: Method,
        @Advice.Return(readOnly = true) returnValue: Any?
    ) {
        RenderedValueInvocationContextHolder.renderedValueInvocationContext().recordInvocation(method, returnValue)
    }
}