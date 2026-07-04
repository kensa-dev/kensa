// Snippet source for kensa.dev/docs/api/outputs.md — Kotlin tabs
package apidocs

import dev.kensa.fixture.FixtureContainer
import dev.kensa.fixture.FixtureRegistry.registerFixtures
import dev.kensa.fixture.fixture
import dev.kensa.junit.KensaTest
import dev.kensa.kotest.WithKotest
import dev.kensa.outputs.capturedOutput
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class HttpResponse(val statusCode: Int)
class PaymentRequest
class PaymentResult(val transactionId: String)

class PaymentGateway {
    fun submit(request: PaymentRequest): HttpResponse = HttpResponse(200)
    fun submitForResult(request: PaymentRequest): PaymentResult = PaymentResult("TX-1")
}

class HttpClient {
    fun post(path: String, body: String): HttpResponse = HttpResponse(200)
}

object PaymentFixtures : FixtureContainer {
    val paymentRequest = fixture("Payment Request") { PaymentRequest() }
}

// Define once, e.g. as a companion object field or top-level val
val response = capturedOutput<HttpResponse>("response")

class PaymentTest : KensaTest, WithKotest {

    init {
        registerFixtures(PaymentFixtures)
    }

    private val paymentGateway = PaymentGateway()
    private val httpClient = HttpClient()
    private val body = "{}"

    @Test
    fun `payment is accepted`() {
        given { /* ... */ }
        whenever { ctx ->
            ctx.outputs[response] = paymentGateway.submit(ctx.fixtures[PaymentFixtures.paymentRequest])
        }
        then({ ctx -> ctx.outputs[response] }) {
            statusCode shouldBe 200
        }
    }

    @Test
    fun `string keys`() {
        whenever { ctx ->
            ctx.outputs.put("response", httpClient.post("/payments", body))
        }
        then({ ctx -> ctx.outputs["response"] as HttpResponse }) {
            statusCode shouldBe 200
        }
    }

    @Test
    fun highlighting() {
        val transactionId = capturedOutput<String>("transaction id", highlighted = true)

        whenever { ctx ->
            val result = paymentGateway.submitForResult(ctx.fixtures[PaymentFixtures.paymentRequest])
            ctx.outputs[transactionId] = result.transactionId
        }
    }
}
