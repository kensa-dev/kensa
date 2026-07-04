// Snippet source for kensa.dev/docs/quickstart/kotlin-quickstart.md §4 (chaining) and §5 (interactions)
package quickstart

import dev.kensa.Action
import dev.kensa.ActionContext
import dev.kensa.GivensContext
import dev.kensa.StateCollector
import dev.kensa.junit.KensaTest
import dev.kensa.kotest.WithKotest
import dev.kensa.state.CapturedInteractionBuilder.Companion.from
import dev.kensa.state.Party
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import org.junit.jupiter.api.Test
import quickstart.LoanParties.Client
import quickstart.LoanParties.LoanProcessor

enum class LoanParties : Party {
    Client, LoanProcessor;

    override fun asString(): String = name
}

class KotlinChainAndInteractionsTest : KensaTest, WithKotest {

    private val service = LoanService()
    private lateinit var applicant: Applicant
    private lateinit var result: LoanResult

    @Test
    fun canApproveLoanWithUnderwritingApproval() {
        given(anApplicantWithGoodCredit())
        and(anApprovalFromUnderwriting())

        whenever(theLoanServiceProcessesTheApplication())

        then(theLoanResult()) { status shouldBe LoanStatus.Approved }
        and(theLoanReference()) { shouldStartWith("LN-") }
    }

    private fun anApplicantWithGoodCredit() = Action<GivensContext> {
        applicant = Applicant("Alice", creditScore = 750, amount = 10_000)
    }

    private fun anApprovalFromUnderwriting() = Action<GivensContext> {
        // configure your underwriting stub here
    }

    // §5 — Record Interactions
    private fun theLoanServiceProcessesTheApplication() = Action<ActionContext> { ctx ->
        ctx.interactions.capture(
            from(Client).to(LoanProcessor).with("process(${applicant.name})", "Loan Request")
        )

        result = service.process(applicant)

        ctx.interactions.capture(
            from(LoanProcessor).to(Client).with(result, "Loan Result")
        )
    }

    private fun theLoanResult() = StateCollector { result }

    private fun theLoanReference() = StateCollector { result.reference }
}
