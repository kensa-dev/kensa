package dev.kensa.compiler

import dev.kensa.context.ExpandableInvocationContext
import dev.kensa.context.ExpandableInvocationContextHolder
import dev.kensa.context.RealExpandableInvocation
import dev.kensa.parse.MethodParameters
import dev.kensa.parse.ParsedExpandableMethod
import example.ExpandableSentences
import example.Service
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify

class ExpandableSentenceIntegrationTest {

    @TestFactory
    fun `member functions invoke the expandable sentence hook`(): List<DynamicTest> {
        val service = mock<Service>()
        val instance = ExpandableSentences(service)
        return listOf(
            expandableCase("foo", invoke = { instance.foo("test arg") }, args = listOf("test arg"), service = service),
            expandableCase("bar", invoke = { instance.bar(42) }, args = listOf(42), service = service),
            expandableCase("longParam", invoke = { instance.longParam(7L) }, args = listOf(7L), service = service),
            expandableCase("booleanParam", invoke = { instance.booleanParam(true) }, args = listOf(true), service = service),
            expandableCase("doubleParam", invoke = { instance.doubleParam(1.5) }, args = listOf(1.5), service = service),
            expandableCase("charParam", invoke = { instance.charParam('z') }, args = listOf('z'), service = service),
            expandableCase("expressionBody", invoke = { instance.expressionBody("e") }, args = listOf("e"), service = service),
            expandableCase(
                "onExtensionReceiver",
                invoke = { with(instance) { "ext".onExtensionReceiver() } },
                args = listOf("ext"),
                service = service
            ),
            expandableCase(
                "withContextParameter",
                invoke = { with("p") { instance.withContextParameter("v") } },
                args = listOf("p", "v"),
                service = service
            )
        )
    }

    private fun expandableCase(
        methodName: String,
        invoke: () -> Unit,
        args: List<Any?>,
        service: Service
    ): DynamicTest = dynamicTest("$methodName(${args.joinToString()})") {
        reset(service)
        val context = ExpandableInvocationContext()
        context.update(mapOf(methodName to ParsedExpandableMethod(methodName, MethodParameters(emptyMap()), emptyList())))
        ExpandableInvocationContextHolder.bindToCurrentThread(context)
        try {
            invoke()
            val invocation = context.nextInvocationFor(methodName)
            invocation.shouldBeInstanceOf<RealExpandableInvocation> {
                it.arguments.toList().shouldContainExactly(args)
            }
            verify(service).call(args.filterNotNull().toTypedArray())
        } finally {
            ExpandableInvocationContextHolder.clearFromThread()
        }
    }
}
