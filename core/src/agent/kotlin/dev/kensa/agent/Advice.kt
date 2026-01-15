package dev.kensa.agent

import dev.kensa.context.NestedInvocationContextHolder.expandableSentenceInvocationContext
import dev.kensa.context.RenderedValueInvocationContextHolder
import dev.kensa.context.RenderedValueInvocationContextHolder.renderedValueInvocationContext
import net.bytebuddy.implementation.bind.annotation.AllArguments
import net.bytebuddy.implementation.bind.annotation.Origin
import net.bytebuddy.implementation.bind.annotation.RuntimeType
import net.bytebuddy.implementation.bind.annotation.SuperCall
import java.lang.reflect.Method
import java.util.concurrent.Callable

// TODO: Investigate why these fail for latest Kotlin (2.2.20)
//object NestedSentenceAdvice {
//    @JvmStatic
//    @Advice.OnMethodEnter
//    fun enter(
//        @Advice.Origin method: Method,
//        @Advice.AllArguments args: Array<Any?>
//    ) {
//        NestedInvocationContextHolder.nestedSentenceInvocationContext().recordInvocation(method, args)
//    }
//}

//object RenderedValueAdvice {
//    @JvmStatic
//    @Advice.OnMethodExit
//    fun exit(
//        @Advice.Origin method: Method,
//        @Advice.Return(readOnly = true) returnValue: Any?
//    ) {
//        RenderedValueInvocationContextHolder.renderedValueInvocationContext().recordInvocation(method, returnValue)
//    }
//}

object ExpandableSentenceInterceptor {
    @JvmStatic
    @RuntimeType
    fun intercept(@Origin method: Method, @AllArguments args: Array<Any?>, @SuperCall superCall: Callable<Any?>): Any? =
        superCall.call().also {
            expandableSentenceInvocationContext().recordInvocation(method, args)
        }
}

object RenderedValueInterceptor {
    @JvmStatic
    @RuntimeType
    fun intercept(@Origin method: Method, @SuperCall superCall: Callable<Any?>): Any? =
        superCall.call().also { returnValue ->
            renderedValueInvocationContext().recordInvocation(method, returnValue)
        }
}