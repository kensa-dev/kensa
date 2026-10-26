package dev.kensa.example

import dev.kensa.Action
import dev.kensa.ActionContext
import dev.kensa.GivensContext
import dev.kensa.example.TradingParty.Broker
import dev.kensa.example.TradingParty.Client
import dev.kensa.state.CapturedInteractionBuilder.Companion.from
import dev.kensa.state.Party
import org.junit.jupiter.api.Test
import java.time.Instant

class KotlinWithInteractionOrderTest : KotlinExampleTest() {

    @Test
    fun ordersInteractionsCapturedInTheSameMillisecondByTimestamp() {
        given(anOpenTradingSession())

        whenever(theBrokerAndTheClientExchangeMessages())
    }

    private fun anOpenTradingSession() = Action<GivensContext> { }

    private fun theBrokerAndTheClientExchangeMessages() = Action<ActionContext> { context ->
        val base = Instant.parse("2026-10-21T10:15:30.123Z")
        context.interactions.capture(from(Client).to(Broker).with("ACK", "Acknowledgement").with(base.plusNanos(2_000)))
        context.interactions.capture(from(Broker).to(Client).with("GBPUSD 1.2710", "Quote").with(base.plusNanos(1_000)))
    }
}

enum class TradingParty : Party {
    Broker, Client;

    override fun asString(): String = name
}
