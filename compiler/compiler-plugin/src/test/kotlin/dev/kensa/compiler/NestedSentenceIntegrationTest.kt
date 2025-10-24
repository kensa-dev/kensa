package dev.kensa.compiler

import dev.kensa.context.NestedInvocationContext
import dev.kensa.context.NestedInvocationContextHolder
import dev.kensa.context.RealNestedInvocation
import dev.kensa.parse.ElementDescriptor
import dev.kensa.parse.MethodParameters
import dev.kensa.parse.ParsedNestedMethod
import example.NestedSentences
import example.Service
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import java.lang.reflect.Method
import kotlin.reflect.KClass

class NestedSentenceIntegrationTest {

    private lateinit var nestedInvocationContext: NestedInvocationContext
    private val service = mock<Service>()

    @BeforeEach
    fun setUp() {
        reset(service)
        nestedInvocationContext = NestedInvocationContext()
        NestedInvocationContextHolder.bindToCurrentThread(nestedInvocationContext)
    }

    @AfterEach
    fun tearDown() {
        NestedInvocationContextHolder.clearFromThread()
    }

    @Test
    fun `simple annotated function compiles and executes`() {
        val methodName = "foo"

        val parsedNestedMethod = mockParsedNestedMethod<NestedSentences>(methodName, String::class)
        nestedInvocationContext.update(mapOf(methodName to parsedNestedMethod))

        val instance = NestedSentences(service)

        instance.foo("test arg")

        val invocation = nestedInvocationContext.nextInvocationFor(methodName)

        invocation.shouldBeInstanceOf<RealNestedInvocation> {
            it.arguments.shouldContainExactly("test arg")
        }

        verify(service).call(arrayOf("test arg"))
    }

    private inline fun <reified T> mockParsedNestedMethod(fnName: String, vararg parameterTypes: KClass<*>): ParsedNestedMethod {
        val klass = T::class.java
        val method: Method = klass.getMethod(fnName, *parameterTypes.map { it.java }.toTypedArray()) ?: throw IllegalArgumentException("No such method: [$fnName] with parameters: ${parameterTypes.joinToString(", ") { it.simpleName!! }}")
        val parameterDescriptors = method.parameters.mapIndexed { i, p -> ElementDescriptor.forParameter(p, p.name, i) }.associateBy { it.name }
        val methodParameters = mock<MethodParameters> { on { descriptors } doReturn parameterDescriptors }

        return mock<ParsedNestedMethod> { on { parameters } doReturn methodParameters }
    }
}