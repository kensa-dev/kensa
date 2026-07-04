// Snippet source for kensa.dev/docs/quickstart/kotlin-quickstart.md §3
package quickstart

import dev.kensa.Action
import dev.kensa.ActionContext
import dev.kensa.GivensContext
import dev.kensa.RenderedValue
import dev.kensa.StateCollector
import dev.kensa.junit.KensaTest
import dev.kensa.kotest.WithKotest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class KotlinJunitQuickstartTest : KensaTest, WithKotest {

    @RenderedValue
    private val applicantName = "Alice"

    @RenderedValue
    private val requestedAmount = 10_000

    private val service = LoanService()
    private lateinit var applicant: Applicant
    private lateinit var result: LoanResult

    @Test
    fun canApproveLoanForApplicantWithGoodCredit() {
        given(anApplicantWithGoodCredit())
        whenever(theLoanServiceProcessesTheApplication())
        then(theLoanResult()) { status shouldBe LoanStatus.Approved }
    }

    @Test
    fun canDeclineLoanForApplicantWithPoorCredit() {
        given(anApplicantWithPoorCredit())
        whenever(theLoanServiceProcessesTheApplication())
        then(theLoanResult()) { status shouldBe LoanStatus.Declined }
    }

    // --- Givens ---

    private fun anApplicantWithGoodCredit() = Action<GivensContext> {
        applicant = Applicant(applicantName, creditScore = 750, amount = requestedAmount)
    }

    private fun anApplicantWithPoorCredit() = Action<GivensContext> {
        applicant = Applicant(applicantName, creditScore = 300, amount = requestedAmount)
    }

    // --- Action ---

    private fun theLoanServiceProcessesTheApplication() = Action<ActionContext> {
        result = service.process(applicant)
    }

    // --- State ---

    private fun theLoanResult() = StateCollector { result }
}
