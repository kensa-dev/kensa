// Snippet source for kensa.dev/docs/api/async-assertions.md — Kotlin/Kotest tabs
package apidocs

import dev.kensa.StateCollector
import dev.kensa.junit.KensaTest
import dev.kensa.kotest.WithKotest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.startWith
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class AsyncAssertionsSnippets : KensaTest, WithKotest {

    private val orderService = OrderService()

    @Test
    fun `order is eventually confirmed`() {
        whenever { orderService.placeOrder() }

        // polls the collector until the block passes (default: 10s timeout, 25ms interval)
        thenEventually(theOrderStatus()) { this shouldBe "CONFIRMED" }
    }

    @Test
    fun `order is eventually confirmed with custom timeout`() {
        whenever { orderService.placeOrder() }

        thenEventually(30.seconds, theOrderStatus()) { this shouldBe "CONFIRMED" }
        andEventually(theOrderReference(), startWith("ORD-"))
    }

    @Test
    fun `order is eventually confirmed with full tuning`() {
        whenever { orderService.placeOrder() }

        thenEventually(
            initialDelay = 500.milliseconds,
            duration = 30.seconds,
            interval = 100.milliseconds,
            collector = theOrderStatus(),
            match = startWith("CONF")
        )
    }

    @Test
    fun `order is never cancelled`() {
        whenever { orderService.placeOrder() }

        // asserts the condition keeps holding for the whole duration (default: 10s)
        thenContinually(5.seconds, theOrderStatus()) { this shouldBe "CONFIRMED" }
    }

    private fun theOrderStatus() = StateCollector { orderService.status() }

    private fun theOrderReference() = StateCollector { orderService.reference() }

    class OrderService {
        fun placeOrder() {}
        fun status(): String = "CONFIRMED"
        fun reference(): String = "ORD-1"
    }
}
