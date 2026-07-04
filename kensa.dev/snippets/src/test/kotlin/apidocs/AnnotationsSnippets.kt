// Snippet source for kensa.dev/docs/api/annotations.md — @ParameterizedTestDescription, @OrgFlow, @OrgFlowMarker
package apidocs

import dev.kensa.OrgFlow
import dev.kensa.OrgFlowMarker
import dev.kensa.ParameterizedTestDescription
import dev.kensa.context.OrgFlowSpec
import dev.kensa.junit.KensaTest
import dev.kensa.kotest.WithKotest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ParameterizedDescriptionSnippets : KensaTest, WithKotest {

    @ParameterizedTest
    @CsvSource(
        "a standard loan is approved, 15000, 36",
        "a high-value loan needs underwriting, 75000, 60",
    )
    fun canProcessLoanApplications(
        @ParameterizedTestDescription description: String,
        amount: Int,
        termMonths: Int,
    ) {
        // ...
    }
}

// --- @OrgFlow ---

class OrgFlowSnippets : KensaTest, WithKotest {

    @OrgFlow(category = "Payments", name = "Card checkout", product = "Web")
    @org.junit.jupiter.api.Test
    fun canCheckOutWithACard() {
        // ...
    }
}

// --- @OrgFlowMarker ---

enum class CheckoutFlow(
    override val category: String,
    override val flowName: String,
    override val attributes: Map<String, String> = emptyMap(),
) : OrgFlowSpec {
    CardCheckout("Payments", "Card checkout"),
}

@OrgFlowMarker
@Target(AnnotationTarget.FUNCTION)
annotation class Flow(val value: CheckoutFlow)

class OrgFlowMarkerSnippets : KensaTest, WithKotest {

    @Flow(CheckoutFlow.CardCheckout)
    @org.junit.jupiter.api.Test
    fun canCheckOutWithACard() {
        // ...
    }
}
