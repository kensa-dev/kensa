package dev.kensa.parse

import dev.kensa.RenderedValue
import dev.kensa.RenderedValueContainer
import dev.kensa.util.allProperties
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class ElementDescriptorTest {

    class Stub
    class Container(@RenderedValue val stub: Stub, val plain: String)

    class Holder {
        @RenderedValueContainer
        val holder = Container(Stub(), "x")

        @RenderedValue
        val other = "y"
    }

    @Suppress("UNUSED_PARAMETER")
    fun target(@RenderedValueContainer useCase: Container, plain: String) {
    }

    @Test
    fun `parameter descriptor exposes container metadata`() {
        val fn = ElementDescriptorTest::class.java.declaredMethods.single { it.name == "target" }
        val containerParam = ElementDescriptor.forParameter(fn.parameters[0], "useCase", 0)
        val plainParam = ElementDescriptor.forParameter(fn.parameters[1], "plain", 1)

        containerParam.isRenderedValueContainer.shouldBeTrue()
        containerParam.containerRenderedMembers.shouldBe(setOf("stub"))
        plainParam.isRenderedValueContainer.shouldBeFalse()
        plainParam.containerRenderedMembers.shouldBeEmpty()
    }

    @Test
    fun `property descriptor exposes container members`() {
        val holderProperty = Holder::class.java.allProperties.single { it.name == "holder" }
        val otherProperty = Holder::class.java.allProperties.single { it.name == "other" }

        ElementDescriptor.forProperty(holderProperty).containerRenderedMembers.shouldBe(setOf("stub"))
        ElementDescriptor.forProperty(otherProperty).containerRenderedMembers.shouldBeEmpty()
    }
}
