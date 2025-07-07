package dev.kensa.agent

import dev.kensa.context.NestedInvocationContextHolder
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