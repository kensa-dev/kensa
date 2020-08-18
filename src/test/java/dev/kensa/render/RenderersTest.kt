package dev.kensa.render

import dev.kensa.util.*
import org.assertj.core.api.Assertions.assertThat
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
        assertThat(renderers.renderValueOnly(null)).isEqualTo("NULL")
    }

    @Test
    internal fun `defaults to object renderer if no specific renderer exists`() {
        assertThat(renderers.renderValueOnly(100)).isEqualTo("100")
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

        assertThat(renderers.renderValueOnly(100)).isEqualTo("<100>")
        assertThat(renderers.renderValueOnly(true)).isEqualTo("<<true>>")
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

        assertThat(renderers.renderValueOnly(100)).isEqualTo("<100>")
        assertThat(renderers.renderValueOnly(true)).isEqualTo("<<true>>")
    }

    @Test
    internal fun `can find renderer for kotlin object specified by interface`() {
        renderers.add(SomeKotlinInterface::class, object : Renderer<SomeKotlinInterface> {
            override fun render(value: SomeKotlinInterface): String {
                return "<${value.renderMe()}>"
            }
        })

        assertThat(renderers.renderValueOnly(SomeKotlinSubClass(field1 = "foo"))).isEqualTo("<foo>")
    }

    @Test
    internal fun `can find renderer for java object specified by interface`() {
        renderers.add(SomeJavaInterface::class, object : Renderer<SomeJavaInterface> {
            override fun render(value: SomeJavaInterface): String {
                return "<${value.renderMe()}>"
            }
        })

        assertThat(renderers.renderValueOnly(SomeJavaSubClass(10, "foo"))).isEqualTo("<foo>")
    }

    @Test
    internal fun `can find renderer for kotlin object specified by superclass`() {
        renderers.add(SomeKotlinSuperClass::class, object : Renderer<SomeKotlinSuperClass> {
            override fun render(value: SomeKotlinSuperClass): String {
                return "<${value.superRenderMe()}>"
            }
        })

        assertThat(renderers.renderValueOnly(SomeKotlinSubClass(666, ""))).isEqualTo("<666>")
    }

    @Test
    internal fun `can find renderer for java object specified by superclass`() {
        renderers.add(SomeJavaSuperClass::class, object : Renderer<SomeJavaSuperClass> {
            override fun render(value: SomeJavaSuperClass): String {
                return "<${value.superRenderMe()}>"
            }
        })

        assertThat(renderers.renderValueOnly(SomeJavaSubClass(666, ""))).isEqualTo("<666>")
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

            assertThat(renderValueOnly(SomeKotlinSubClass(10, "boo"))).isEqualTo("<<<boo>>>")
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

            assertThat(renderValueOnly(SomeJavaSubClass(10, "boo"))).isEqualTo("<<<boo>>>")
        }
    }
}