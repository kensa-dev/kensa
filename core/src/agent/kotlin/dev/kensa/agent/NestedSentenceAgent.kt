package dev.kensa.agent

import dev.kensa.NestedSentence
import net.bytebuddy.ByteBuddy
import net.bytebuddy.agent.builder.AgentBuilder
import net.bytebuddy.asm.Advice
import net.bytebuddy.matcher.ElementMatchers.*
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.Instrumentation
import java.util.function.Function

object NestedSentenceAgent {
    private val DISABLED = net.bytebuddy.dynamic.scaffold.TypeValidation.DISABLED

    @Suppress("UNUSED_PARAMETER")
    @JvmStatic
    fun premain(arguments: String?, instrumentation: Instrumentation) {
        val classLoader = NestedSentenceAgent::class.java.classLoader
        install(
            AgentBuilder.Default(ByteBuddy().with(DISABLED)),
            instrumentation
        ) { builder: AgentBuilder ->
            builder.type(
                declaresMethod(
                    isAnnotatedWith(
                        named(NestedSentence::class.java.name)
                    )
                ),
                `is`(classLoader)
            )
        }
    }

    private fun install(agentBuilder: AgentBuilder, instrumentation: Instrumentation, typeNarrower: Function<AgentBuilder, AgentBuilder.Identified.Narrowable>): ClassFileTransformer {
        // val agentBuilder = agentBuilder.with(AgentBuilder.Listener.StreamWriting.toSystemOut());
        return typeNarrower.apply(agentBuilder)
            .transform { builder, _, _, _, _ ->
                builder.method(isAnnotatedWith(NestedSentence::class.java))
                    .intercept(Advice.to(NestedSentenceAdvice::class.java))
            }
            .installOn(instrumentation)
    }
}
