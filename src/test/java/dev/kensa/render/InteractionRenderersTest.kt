package dev.kensa.render

import dev.kensa.render.Language.PlainText
import dev.kensa.render.Language.Xml
import dev.kensa.util.Attributes
import dev.kensa.util.Attributes.Companion.emptyAttributes
import dev.kensa.util.NamedValue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.text.DecimalFormat

internal class InteractionRenderersTest {
    private lateinit var renderers: Renderers

    @BeforeEach
    internal fun setUp() {
        renderers = Renderers()
    }

    @Test
    fun `defaults to object renderer if no specific renderer exists`() {
        renderers.renderInteraction(100, emptyAttributes())
            .shouldHaveSize(1)
            .first().should {
                it.name shouldBe "Undefined Value"
                it.value shouldBe "100"
                it.language shouldBe PlainText
            }
    }

    @Test
    fun `renders interaction and attributes with specified renderer`() {
        renderers.addInteractionRenderer(Long::class, object : InteractionRenderer<Long> {
            override fun render(value: Long, attributes: Attributes): List<RenderedInteraction> = listOf(
                RenderedInteraction("Normal", value.toString(), language = attributes.get<Language>("language")!!),
                RenderedInteraction("Formatted", DecimalFormat("#,###").format(value))
            )

            override fun renderAttributes(value: Long): List<RenderedAttributes> = listOf(
                RenderedAttributes("", setOf(NamedValue("Is greater than 5", value > 5)), false),
                RenderedAttributes("", setOf(NamedValue("Is greater than 50", value > 50)), false)
            )
        })

        val language = Xml
        renderers.renderInteraction(25L, Attributes.of("language", language))
            .shouldHaveSize(2)
            .shouldContainExactly(
                RenderedInteraction("Normal", "25", language = language),
                RenderedInteraction("Formatted", DecimalFormat("#,###").format(25))
            )

        renderers.renderInteractionAttributes(25L)
            .shouldHaveSize(2)
            .shouldContainExactly(
                RenderedAttributes("", setOf(NamedValue("Is greater than 5", true)), false),
                RenderedAttributes("", setOf(NamedValue("Is greater than 50", false)), false)
            )
    }
}