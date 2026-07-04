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

    private fun theOrderStatus() = StateCollector { orderService.status() }
}
