// Snippet source for kensa.dev/docs/quickstart/testng-quickstart.md §3 Kotlin tab + §5 listener
package quickstart

import dev.kensa.Action
import dev.kensa.ActionContext
import dev.kensa.GivensContext
import dev.kensa.RenderedValue
import dev.kensa.StateCollector
import dev.kensa.kotest.WithKotest
import dev.kensa.testng.KensaTest
import io.kotest.matchers.shouldBe
import org.testng.ISuite
import org.testng.ISuiteListener
import org.testng.annotations.Listeners
import org.testng.annotations.Test

class TestNgKotlinQuickstartTest : KensaTest, WithKotest {

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

// §5 — Adding Your Own TestNG Listeners
class MyAppListener : ISuiteListener {
    override fun onStart(suite: ISuite) {
        // start stubs, register fixtures, call Kensa.konfigure { ... }
    }

    override fun onFinish(suite: ISuite) {
        // close stubs and other resources
    }
}

@Listeners(MyAppListener::class)
abstract class MyAppTest : KensaTest, WithKotest
