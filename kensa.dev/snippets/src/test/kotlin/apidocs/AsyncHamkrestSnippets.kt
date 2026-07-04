// Snippet source for kensa.dev/docs/api/async-assertions.md — Hamcrest/HamKrest section
package apidocs

import com.natpryce.hamkrest.equalTo
import dev.kensa.StateCollector
import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.junit.KensaTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class AsyncHamkrestSnippets : KensaTest, WithHamkrest {

    private val orderService = AsyncAssertionsSnippets.OrderService()

    @Test
    fun `order is eventually confirmed`() {
        whenever { orderService.placeOrder() }

        thenEventually(theOrderStatus(), equalTo("CONFIRMED"))
        thenContinually(5.seconds, theOrderStatus(), equalTo("CONFIRMED"))
    }

    @Test
    fun `order is confirmed and reference assigned within one window`() {
        whenever { orderService.placeOrder() }

        thenEventually(10.seconds) {
            then(theOrderStatus(), equalTo("CONFIRMED"))
            and(theOrderReference(), equalTo("ORD-1"))
        }
    }

    @Test
    fun `order stays confirmed and reference stays stable`() {
        whenever { orderService.placeOrder() }

        thenContinually(5.seconds) {
            then(theOrderStatus(), equalTo("CONFIRMED"))
            and(theOrderReference(), equalTo("ORD-1"))
        }
    }

    private fun theOrderStatus() = StateCollector { orderService.status() }

    private fun theOrderReference() = StateCollector { orderService.reference() }
}
