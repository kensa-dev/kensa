package dev.kensa.render

import dev.kensa.example.SomeJavaInterface
import dev.kensa.example.SomeJavaSubClass
import dev.kensa.example.SomeJavaSuperClass
import dev.kensa.example.SomeKotlinInterface
import dev.kensa.example.SomeKotlinSubClass
import dev.kensa.example.SomeKotlinSuperClass
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
internal class ValueRenderersTest {
    private lateinit var renderers: Renderers

    @BeforeEach
    internal fun setUp() {
        renderers = Renderers()
    }

    @Test
    internal fun `handles null`() {
        renderers.renderValue(null) shouldBe "NULL"
    }

    @Test
    internal fun `defaults to object renderer if no specific renderer exists`() {
        renderers.renderValue(100) shouldBe "100"
    }

    @Test
    internal fun `can find value renderer for simple kotlin type`() {
        renderers.addValueRenderer(Int::class) { value -> "<$value>" }
        renderers.addValueRenderer(Boolean::class) { value -> "<<$value>>" }

        renderers.renderValue(100) shouldBe "<100>"
        renderers.renderValue(true) shouldBe "<<true>>"
    }

    @Test
    internal fun `can find value renderer for simple java type`() {
        renderers.addValueRenderer(Integer::class.java) { value -> "<$value>" }
        renderers.addValueRenderer(java.lang.Boolean::class.java) { value -> "<<$value>>" }

        renderers.renderValue(100) shouldBe "<100>"
        renderers.renderValue(true) shouldBe "<<true>>"
    }

    @Test
    internal fun `can find value renderer for kotlin object specified by interface`() {
        renderers.addValueRenderer(SomeKotlinInterface::class) { value -> "<${value.renderMe()}>" }

        renderers.renderValue(SomeKotlinSubClass(field1 = "foo")) shouldBe "<foo>"
    }

    @Test
    internal fun `can find value renderer for java object specified by interface`() {
        renderers.addValueRenderer(SomeJavaInterface::class) { value -> "<${value.renderMe()}>" }

        renderers.renderValue(SomeJavaSubClass(10, "foo")) shouldBe "<foo>"
    }

    @Test
    internal fun `can find value renderer for kotlin object specified by superclass`() {
        renderers.addValueRenderer(SomeKotlinSuperClass::class) { value -> "<${value.superRenderMe()}>" }

        renderers.renderValue(SomeKotlinSubClass(666, "")) shouldBe "<666>"
    }

    @Test
    internal fun `can find renderer for java object specified by superclass`() {
        renderers.addValueRenderer(SomeJavaSuperClass::class) { value -> "<${value.superRenderMe()}>" }

        renderers.renderValue(SomeJavaSubClass(666, "")) shouldBe "<666>"
    }

    @Test
    fun `can render a list using default list renderer`() {
        val list = listOf("1", "2", "3", "4", "5", null)

        renderers.addValueRenderer(String::class) { value -> "<$value>" }

        renderers.renderValue(list) shouldBe "[<1>, <2>, <3>, <4>, <5>, NULL]"
    }

    @Test
    fun `can render a list using default list renderer with custom format`() {
        val list = listOf("1", "2", "3", "4", "5")

        renderers.setListRendererFormat(ListRendererFormat(",", "(", ")"))
        renderers.addValueRenderer(String::class) { value -> "<$value>" }

        renderers.renderValue(list) shouldBe "(<1>,<2>,<3>,<4>,<5>)"
    }

    @Test
    fun `can render a list using custom list renderer`() {
        val list = listOf("1", "2", "3", "4", "5")

        renderers.setListRenderer { theList -> theList.joinToString { "*$it*" } }
        renderers.addValueRenderer(String::class) { value -> "<$value>" }

        renderers.renderValue(list) shouldBe "*1*, *2*, *3*, *4*, *5*"
    }

    @Test
    internal fun `uses most specific value renderer when multiple value renderers for kotlin hierarchy specified`() {
        with(renderers) {
            addValueRenderer(SomeKotlinSuperClass::class) { value -> "<${value.superRenderMe()}>" }
            addValueRenderer(SomeKotlinSubClass::class) { value -> "<<<${value.renderMe()}>>>" }
            addValueRenderer(SomeKotlinInterface::class) { value -> "<<$value>>" }

            renderValue(SomeKotlinSubClass(10, "boo")) shouldBe "<<<boo>>>"
        }
    }

    @Test
    internal fun `uses most specific value renderer when multiple value renderers for java hierarchy specified`() {
        with(renderers) {
            addValueRenderer(SomeJavaSuperClass::class) { value -> "<${value.superRenderMe()}>" }
            addValueRenderer(SomeJavaSubClass::class) { value -> "<<<${value.renderMe()}>>>" }
            addValueRenderer(SomeJavaInterface::class) { value -> "<<$value>>" }

            renderValue(SomeJavaSubClass(10, "boo")) shouldBe "<<<boo>>>"
        }
    }
}