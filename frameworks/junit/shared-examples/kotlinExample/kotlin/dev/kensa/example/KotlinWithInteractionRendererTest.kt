package dev.kensa.example

import dev.kensa.Action
import dev.kensa.ActionContext
import dev.kensa.GivensContext
import dev.kensa.render.InteractionRenderer
import dev.kensa.render.Language
import dev.kensa.render.RenderedAttributes
import dev.kensa.render.RenderedInteraction
import dev.kensa.util.Attributes
import dev.kensa.util.NamedValue
import org.junit.jupiter.api.Test

class KotlinWithInteractionRendererTest : KotlinExampleTest() {

    @Test
    fun rendersACapturedInteractionWithTheRegisteredRenderer() {
        given(anOpenTradingSession())

        whenever(theRateServiceQuotes())
    }

    private fun anOpenTradingSession() = Action<GivensContext> { }

    private fun theRateServiceQuotes() = Action<ActionContext> { context ->
        context.interactions.put("Rate Quote Response", aQuote)
    }

    private val aQuote = RateQuote(
        "GBPUSD",
        "1.2710",
        200,
        listOf("Content-Type" to "application/json", "X-Quote-Id" to "Q-17")
    )
}

data class RateQuote(val currency: String, val rate: String, val statusCode: Int, val headers: List<Pair<String, String>>)

object RateQuoteRenderer : InteractionRenderer<RateQuote> {
    override fun render(value: RateQuote, attributes: Attributes): List<RenderedInteraction> = listOf(
        RenderedInteraction("Quote Body", """{"currency": "${value.currency}", "rate": "${value.rate}"}""", Language.Json),
        RenderedInteraction("Summary", "${value.currency} quoted at ${value.rate}")
    )

    override fun renderAttributes(value: RateQuote): List<RenderedAttributes> = listOf(
        RenderedAttributes("Status", setOf(NamedValue("Code", value.statusCode))),
        RenderedAttributes("Headers", value.headers.map { NamedValue(it.first, it.second) }.toSet())
    )
}
