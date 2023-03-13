package dev.kensa.render

import dev.kensa.util.*
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
internal class RenderersTest {
    private lateinit var renderers: Renderers

    @BeforeEach
    internal fun setUp() {
        renderers = Renderers()
    }

    @Test
    internal fun `handles null`() {
        renderers.renderValueOnly(null) shouldBe "NULL"
    }

    @Test
    internal fun `defaults to object renderer if no specific renderer exists`() {
        renderers.renderValueOnly(100) shouldBe "100"
    }

    @Test
    internal fun `can find renderer for simple kotlin type`() {
        renderers.add(Int::class, object : Renderer<Int> {
            override fun render(value: Int): String {
                return "<$value>"
            }
        })
        renderers.add(Boolean::class, object : Renderer<Boolean> {
            override fun render(value: Boolean): String {
                return "<<$value>>"
            }
        })

        renderers.renderValueOnly(100) shouldBe "<100>"
        renderers.renderValueOnly(true) shouldBe "<<true>>"
    }

    @Test
    internal fun `can find renderer for simple java type`() {
        renderers.add(Integer::class.java, object : Renderer<Integer> {
            override fun render(value: Integer): String {
                return "<$value>"
            }
        })
        renderers.add(java.lang.Boolean::class.java, object : Renderer<java.lang.Boolean> {
            override fun render(value: java.lang.Boolean): String {
                return "<<$value>>"
            }
        })

        renderers.renderValueOnly(100) shouldBe "<100>"
        renderers.renderValueOnly(true) shouldBe "<<true>>"
    }

    @Test
    internal fun `can find renderer for kotlin object specified by interface`() {
        renderers.add(SomeKotlinInterface::class, object : Renderer<SomeKotlinInterface> {
            override fun render(value: SomeKotlinInterface): String {
                return "<${value.renderMe()}>"
            }
        })

        renderers.renderValueOnly(SomeKotlinSubClass(field1 = "foo")) shouldBe "<foo>"
    }

    @Test
    internal fun `can find renderer for java object specified by interface`() {
        renderers.add(SomeJavaInterface::class, object : Renderer<SomeJavaInterface> {
            override fun render(value: SomeJavaInterface): String {
                return "<${value.renderMe()}>"
            }
        })

        renderers.renderValueOnly(SomeJavaSubClass(10, "foo")) shouldBe "<foo>"
    }

    @Test
    internal fun `can find renderer for kotlin object specified by superclass`() {
        renderers.add(SomeKotlinSuperClass::class, object : Renderer<SomeKotlinSuperClass> {
            override fun render(value: SomeKotlinSuperClass): String {
                return "<${value.superRenderMe()}>"
            }
        })

        renderers.renderValueOnly(SomeKotlinSubClass(666, "")) shouldBe "<666>"
    }

    @Test
    internal fun `can find renderer for java object specified by superclass`() {
        renderers.add(SomeJavaSuperClass::class, object : Renderer<SomeJavaSuperClass> {
            override fun render(value: SomeJavaSuperClass): String {
                return "<${value.superRenderMe()}>"
            }
        })

        renderers.renderValueOnly(SomeJavaSubClass(666, "")) shouldBe "<666>"
    }

    @Test
    internal fun `uses most specific renderer when multiple renderers for kotlin hierarchy specified`() {
        with(renderers) {
            add(SomeKotlinSuperClass::class, object : Renderer<SomeKotlinSuperClass> {
                override fun render(value: SomeKotlinSuperClass): String {
                    return "<${value.superRenderMe()}>"
                }
            })
            add(SomeKotlinSubClass::class, object : Renderer<SomeKotlinSubClass> {
                override fun render(value: SomeKotlinSubClass): String {
                    return "<<<${value.renderMe()}>>>"
                }
            })
            add(SomeKotlinInterface::class, object : Renderer<SomeKotlinInterface> {
                override fun render(value: SomeKotlinInterface): String {
                    return "<<$value>>"
                }
            })

            renderValueOnly(SomeKotlinSubClass(10, "boo")) shouldBe "<<<boo>>>"
        }
    }

    @Test
    internal fun `uses most specific renderer when multiple renderers for java hierarchy specified`() {
        with(renderers) {
            add(SomeJavaSuperClass::class, object : Renderer<SomeJavaSuperClass> {
                override fun render(value: SomeJavaSuperClass): String {
                    return "<${value.superRenderMe()}>"
                }
            })
            add(SomeJavaSubClass::class, object : Renderer<SomeJavaSubClass> {
                override fun render(value: SomeJavaSubClass): String {
                    return "<<<${value.renderMe()}>>>"
                }
            })
            add(SomeJavaInterface::class, object : Renderer<SomeJavaInterface> {
                override fun render(value: SomeJavaInterface): String {
                    return "<<$value>>"
                }
            })

            renderValueOnly(SomeJavaSubClass(10, "boo")) shouldBe "<<<boo>>>"
        }
    }
}