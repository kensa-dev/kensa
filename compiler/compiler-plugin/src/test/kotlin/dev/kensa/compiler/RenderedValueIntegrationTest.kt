package dev.kensa.compiler

import dev.kensa.context.RealRenderedValueInvocation
import dev.kensa.context.RenderedValueInvocationContext
import dev.kensa.context.RenderedValueInvocationContextHolder
import example.RenderedValues
import example.Service
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify

class RenderedValueIntegrationTest {

    private lateinit var renderedValueInvocationContext: RenderedValueInvocationContext
    private val service = mock<Service>()

    @BeforeEach
    fun setUp() {
        reset(service)
        renderedValueInvocationContext = RenderedValueInvocationContext()
        RenderedValueInvocationContextHolder.bindToCurrentThread(renderedValueInvocationContext)
    }

    @AfterEach
    fun tearDown() {
        RenderedValueInvocationContextHolder.clearFromThread()
    }

    @Test
    fun `simple annotated function compiles and executes`() {
        val methodName = "foo"

        val instance = RenderedValues(service)

        instance.foo("test-arg")

        val invocation = renderedValueInvocationContext.nextInvocationFor(methodName)

        invocation.shouldBeInstanceOf<RealRenderedValueInvocation> {
            it.returnValue shouldBe "foo-test-arg"
        }

        verify(service).call(arrayOf("test-arg"))
    }

    @Test
    fun `expression function compiles and executes`() {
        val methodName = "bar"

        val instance = RenderedValues(service)

        instance.bar(10)

        val invocation = renderedValueInvocationContext.nextInvocationFor(methodName)

        invocation.shouldBeInstanceOf<RealRenderedValueInvocation> {
            it.returnValue shouldBe 11
        }

        verify(service).call(arrayOf(11))
    }
}