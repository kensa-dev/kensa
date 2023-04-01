package dev.kensa.render

import dev.kensa.util.*
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
        renderers.addValueRenderer(Int::class, object : ValueRenderer<Int> {
            override fun render(value: Int): String {
                return "<$value>"
            }
        })
        renderers.addValueRenderer(Boolean::class, object : ValueRenderer<Boolean> {
            override fun render(value: Boolean): String {
                return "<<$value>>"
            }
        })

        renderers.renderValue(100) shouldBe "<100>"
        renderers.renderValue(true) shouldBe "<<true>>"
    }

    @Test
    internal fun `can find value renderer for simple java type`() {
        renderers.addValueRenderer(Integer::class.java, object : ValueRenderer<Integer> {
            override fun render(value: Integer): String {
                return "<$value>"
            }
        })
        renderers.addValueRenderer(java.lang.Boolean::class.java, object : ValueRenderer<java.lang.Boolean> {
            override fun render(value: java.lang.Boolean): String {
                return "<<$value>>"
            }
        })

        renderers.renderValue(100) shouldBe "<100>"
        renderers.renderValue(true) shouldBe "<<true>>"
    }

    @Test
    internal fun `can find value renderer for kotlin object specified by interface`() {
        renderers.addValueRenderer(SomeKotlinInterface::class, object : ValueRenderer<SomeKotlinInterface> {
            override fun render(value: SomeKotlinInterface): String {
                return "<${value.renderMe()}>"
            }
        })

        renderers.renderValue(SomeKotlinSubClass(field1 = "foo")) shouldBe "<foo>"
    }

    @Test
    internal fun `can find value renderer for java object specified by interface`() {
        renderers.addValueRenderer(SomeJavaInterface::class, object : ValueRenderer<SomeJavaInterface> {
            override fun render(value: SomeJavaInterface): String {
                return "<${value.renderMe()}>"
            }
        })

        renderers.renderValue(SomeJavaSubClass(10, "foo")) shouldBe "<foo>"
    }

    @Test
    internal fun `can find value renderer for kotlin object specified by superclass`() {
        renderers.addValueRenderer(SomeKotlinSuperClass::class, object : ValueRenderer<SomeKotlinSuperClass> {
            override fun render(value: SomeKotlinSuperClass): String {
                return "<${value.superRenderMe()}>"
            }
        })

        renderers.renderValue(SomeKotlinSubClass(666, "")) shouldBe "<666>"
    }

    @Test
    internal fun `can find renderer for java object specified by superclass`() {
        renderers.addValueRenderer(SomeJavaSuperClass::class, object : ValueRenderer<SomeJavaSuperClass> {
            override fun render(value: SomeJavaSuperClass): String {
                return "<${value.superRenderMe()}>"
            }
        })

        renderers.renderValue(SomeJavaSubClass(666, "")) shouldBe "<666>"
    }

    @Test
    internal fun `uses most specific value renderer when multiple value renderers for kotlin hierarchy specified`() {
        with(renderers) {
            addValueRenderer(SomeKotlinSuperClass::class, object : ValueRenderer<SomeKotlinSuperClass> {
                override fun render(value: SomeKotlinSuperClass): String {
                    return "<${value.superRenderMe()}>"
                }
            })
            addValueRenderer(SomeKotlinSubClass::class, object : ValueRenderer<SomeKotlinSubClass> {
                override fun render(value: SomeKotlinSubClass): String {
                    return "<<<${value.renderMe()}>>>"
                }
            })
            addValueRenderer(SomeKotlinInterface::class, object : ValueRenderer<SomeKotlinInterface> {
                override fun render(value: SomeKotlinInterface): String {
                    return "<<$value>>"
                }
            })

            renderValue(SomeKotlinSubClass(10, "boo")) shouldBe "<<<boo>>>"
        }
    }

    @Test
    internal fun `uses most specific value renderer when multiple value renderers for java hierarchy specified`() {
        with(renderers) {
            addValueRenderer(SomeJavaSuperClass::class, object : ValueRenderer<SomeJavaSuperClass> {
                override fun render(value: SomeJavaSuperClass): String {
                    return "<${value.superRenderMe()}>"
                }
            })
            addValueRenderer(SomeJavaSubClass::class, object : ValueRenderer<SomeJavaSubClass> {
                override fun render(value: SomeJavaSubClass): String {
                    return "<<<${value.renderMe()}>>>"
                }
            })
            addValueRenderer(SomeJavaInterface::class, object : ValueRenderer<SomeJavaInterface> {
                override fun render(value: SomeJavaInterface): String {
                    return "<<$value>>"
                }
            })

            renderValue(SomeJavaSubClass(10, "boo")) shouldBe "<<<boo>>>"
        }
    }
}