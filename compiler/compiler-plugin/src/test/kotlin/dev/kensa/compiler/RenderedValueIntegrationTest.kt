package dev.kensa.compiler

import dev.kensa.context.RealRenderedValueInvocation
import dev.kensa.context.RenderedValueInvocationContext
import dev.kensa.context.RenderedValueInvocationContextHolder
import example.RenderedValues
import example.Service
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify

class RenderedValueIntegrationTest {

    @TestFactory
    fun `member functions invoke the rendered value hook`(): List<DynamicTest> {
        val service = mock<Service>()
        val instance = RenderedValues(service)
        return listOf(
            renderedValueCase("foo", expected = "foo-x", invoke = { instance.foo("x") }, args = arrayOf("x"), service = service),
            renderedValueCase("bar", expected = 11, invoke = { instance.bar(10) }, args = arrayOf(11), service = service),
            renderedValueCase("unitReturn", expected = Unit, invoke = { instance.unitReturn("u") }, args = arrayOf("u"), service = service),
            renderedValueCase("nullableReturn", expected = "v", invoke = { instance.nullableReturn("v") }, args = arrayOf("v"), service = service),
            renderedValueCase("nullableReturn", expected = null, invoke = { instance.nullableReturn("") }, args = arrayOf(""), service = service),
            renderedValueCase("longReturn", expected = 11L, invoke = { instance.longReturn(10L) }, args = arrayOf(10L), service = service),
            renderedValueCase("booleanReturn", expected = false, invoke = { instance.booleanReturn(true) }, args = arrayOf(true), service = service),
            renderedValueCase("doubleReturn", expected = 4.0, invoke = { instance.doubleReturn(2.0) }, args = arrayOf(2.0), service = service),
            renderedValueCase("charReturn", expected = 'A', invoke = { instance.charReturn('a') }, args = arrayOf('a'), service = service),
            renderedValueCase("expandable", expected = "expandable-e", invoke = { instance.expandable("e") }, args = arrayOf("e"), service = service),
            renderedValueCase(
                "onExtensionReceiver",
                expected = 6,
                invoke = { with(instance) { 3.onExtensionReceiver() } },
                args = arrayOf(6),
                service = service
            ),
            renderedValueCase(
                "withContextParameter",
                expected = "p-v",
                invoke = { with("p") { instance.withContextParameter("v") } },
                args = arrayOf("p", "v"),
                service = service
            )
        )
    }

    private fun renderedValueCase(
        methodName: String,
        expected: Any?,
        invoke: () -> Unit,
        args: Array<Any>,
        service: Service
    ): DynamicTest = dynamicTest("$methodName -> $expected") {
        reset(service)
        val context = RenderedValueInvocationContext()
        RenderedValueInvocationContextHolder.bindToCurrentThread(context)
        try {
            invoke()
            val invocation = context.nextInvocationFor(methodName)
            invocation.shouldBeInstanceOf<RealRenderedValueInvocation> {
                it.returnValue shouldBe expected
            }
            verify(service).call(args)
        } finally {
            RenderedValueInvocationContextHolder.clearFromThread()
        }
    }
}
