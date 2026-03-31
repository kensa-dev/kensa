package dev.kensa.context

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import java.lang.reflect.Method

class RenderedValueInvocationContextTest {
    private val context = RenderedValueInvocationContext()

    private fun method(name: String): Method =
        RenderedValueInvocationContextTest::class.java.declaredMethods.first { it.name == name }

    private fun placeholder() = Unit

    @Test
    fun `returns NoRenderedValueInvocation when no invocations recorded`() {
        context.nextInvocationFor("placeholder") shouldBe NoRenderedValueInvocation
    }

    @Test
    fun `returns recorded value for a single invocation`() {
        context.recordInvocation(method("placeholder"), "hello")

        val result = context.nextInvocationFor("placeholder")

        result.shouldBeInstanceOf<RealRenderedValueInvocation>().returnValue shouldBe "hello"
    }

    @Test
    fun `consumes invocations in order`() {
        context.recordInvocation(method("placeholder"), "first")
        context.recordInvocation(method("placeholder"), "second")

        context.nextInvocationFor("placeholder").shouldBeInstanceOf<RealRenderedValueInvocation>().returnValue shouldBe "first"
        context.nextInvocationFor("placeholder").shouldBeInstanceOf<RealRenderedValueInvocation>().returnValue shouldBe "second"
    }

    @Test
    fun `returns NoRenderedValueInvocation when invocations exhausted`() {
        context.recordInvocation(method("placeholder"), "only")

        context.nextInvocationFor("placeholder")
        val result = context.nextInvocationFor("placeholder")

        result shouldBe NoRenderedValueInvocation
    }

    @Test
    fun `returns NoRenderedValueInvocation for unknown method name`() {
        context.recordInvocation(method("placeholder"), "value")

        context.nextInvocationFor("nonExistent") shouldBe NoRenderedValueInvocation
    }

    @Test
    fun `handles null return values`() {
        context.recordInvocation(method("placeholder"), null)

        context.nextInvocationFor("placeholder").returnValue.shouldBeNull()
    }
}