package dev.kensa.example

import dev.kensa.Action
import dev.kensa.ActionContext
import dev.kensa.ExpandableRenderedValue
import dev.kensa.GivensContext
import dev.kensa.RenderedValueStyle
import org.junit.jupiter.api.Test

class KotlinWithTabularRenderedValueTest : KotlinExampleTest() {

    @Test
    fun rendersATableFromPairs() {
        given(anOpenTradingSession())

        whenever(theBrokerPublishes(theQuotedRates()))
    }

    @Test
    fun rendersATableFromTriples() {
        given(anOpenTradingSession())

        whenever(theBrokerPublishes(theSettlementWindows()))
    }

    @Test
    fun rendersATableFromIterables() {
        given(anOpenTradingSession())

        whenever(theBrokerPublishes(theAuditTrail()))
    }

    @Test
    fun rendersATableFromAnArray() {
        given(anOpenTradingSession())

        whenever(theBrokerPublishes(theClearedTrades()))
    }

    @Test
    fun rendersATableWithNoHeaders() {
        given(anOpenTradingSession())

        whenever(theBrokerPublishes(theUnlabelledRates()))
    }

    @ExpandableRenderedValue(renderAs = RenderedValueStyle.Tabular, headers = ["Currency", "Rate"])
    private fun theQuotedRates(): List<Pair<String, String>> = listOf("GBPUSD" to "1.2710", "EURUSD" to "1.0885")

    @ExpandableRenderedValue(renderAs = RenderedValueStyle.Tabular, headers = ["Currency", "Cutoff", "Days"])
    private fun theSettlementWindows(): List<Triple<String, String, Int>> =
        listOf(Triple("GBPUSD", "16:00", 2), Triple("EURUSD", "17:00", 2))

    @ExpandableRenderedValue(renderAs = RenderedValueStyle.Tabular, headers = ["Event", "Actor", "At"])
    private fun theAuditTrail(): List<List<String>> = listOf(
        listOf("Quoted", "Broker", "09:00"),
        listOf("Accepted", "Client", "09:01")
    )

    @ExpandableRenderedValue(renderAs = RenderedValueStyle.Tabular, headers = ["Trade", "Notional"])
    private fun theClearedTrades(): Array<Array<String>> = arrayOf(
        arrayOf("TRD-1", "1000000"),
        arrayOf("TRD-2", "250000")
    )

    @ExpandableRenderedValue(renderAs = RenderedValueStyle.Tabular)
    private fun theUnlabelledRates(): List<Pair<String, String>> = listOf("GBPUSD" to "1.2710", "EURUSD" to "1.0885")

    private fun theBrokerPublishes(rates: Any) = Action<ActionContext> { }

    private fun anOpenTradingSession() = Action<GivensContext> { }
}
