package dev.kensa.compiler

import dev.kensa.context.ExpandableInvocationContext
import dev.kensa.context.ExpandableInvocationContextHolder
import dev.kensa.context.RealExpandableInvocation
import dev.kensa.parse.ElementDescriptor
import dev.kensa.parse.MethodParameters
import dev.kensa.parse.ParsedExpandableMethod
import example.ExpandableSentences
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

class ExpandableSentenceIntegrationTest {

    private lateinit var expandableInvocationContext: ExpandableInvocationContext
    private val service = mock<Service>()

    @BeforeEach
    fun setUp() {
        reset(service)
        expandableInvocationContext = ExpandableInvocationContext()
        ExpandableInvocationContextHolder.bindToCurrentThread(expandableInvocationContext)
    }

    @AfterEach
    fun tearDown() {
        ExpandableInvocationContextHolder.clearFromThread()
    }

    @Test
    fun `simple annotated function compiles and executes`() {
        val methodName = "foo"

        val parsedNestedMethod = mockParsedNestedMethod<ExpandableSentences>(methodName, String::class)
        expandableInvocationContext.update(mapOf(methodName to parsedNestedMethod))

        val instance = ExpandableSentences(service)

        instance.foo("test arg")

        val invocation = expandableInvocationContext.nextInvocationFor(methodName)

        invocation.shouldBeInstanceOf<RealExpandableInvocation> {
            it.arguments.shouldContainExactly("test arg")
        }

        verify(service).call(arrayOf("test arg"))
    }

    private inline fun <reified T> mockParsedNestedMethod(fnName: String, vararg parameterTypes: KClass<*>): ParsedExpandableMethod {
        val klass = T::class.java
        val method: Method = klass.getMethod(fnName, *parameterTypes.map { it.java }.toTypedArray()) ?: throw IllegalArgumentException("No such method: [$fnName] with parameters: ${parameterTypes.joinToString(", ") { it.simpleName!! }}")
        val parameterDescriptors = method.parameters.mapIndexed { i, p -> ElementDescriptor.forParameter(p, p.name, i) }.associateBy { it.name }
        val methodParameters = mock<MethodParameters> { on { descriptors } doReturn parameterDescriptors }

        return mock<ParsedExpandableMethod> { on { parameters } doReturn methodParameters }
    }
}
