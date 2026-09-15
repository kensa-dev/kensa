// Snippet source for kensa.dev/docs/api/assertion-bridges.md — Kotlin examples
package apidocs

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.startsWith
import dev.kensa.StateCollector
import dev.kensa.assertj.StringStateCollector
import dev.kensa.assertj.WithAssertJ
import dev.kensa.hamcrest.WithHamcrest
import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.junit.KensaTest
import dev.kensa.kotest.WithKotest
import dev.kensa.kotest.kotestSetupStep
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.startWith
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import quickstart.Applicant
import quickstart.LoanResult
import quickstart.LoanService
import quickstart.LoanStatus
import kotlin.time.Duration.Companion.seconds

class KotestBridgeSnippets : KensaTest, WithKotest {

    private val service = LoanService()
    private lateinit var applicant: Applicant
    private lateinit var result: LoanResult

    @Test
    fun `loan is approved`() {
        given(anApplicantWithGoodCredit())
        whenever(theLoanServiceProcessesTheApplication())
        then(theLoanResult()) { status shouldBe LoanStatus.Approved }
        and(theLoanReference(), startWith("LN-"))
    }

    @Test
    fun `loan is eventually approved`() {
        given(anApplicantWithGoodCredit())
        whenever(theLoanServiceProcessesTheApplication())
        thenEventually(theLoanResult()) { status shouldBe LoanStatus.Approved }
        thenContinually(2.seconds, theLoanReference(), startWith("LN-"))
    }

    @Test
    fun `loan is approved during setup`() {
        given(anApprovedLoan())
        then(theLoanReference(), startWith("LN-"))
    }

    private fun anApprovedLoan() = kotestSetupStep {
        given { applicant = Applicant("Alice", creditScore = 750, amount = 10_000) }
        action { result = service.process(applicant) }
        then(theLoanResult()) { status shouldBe LoanStatus.Approved }
    }

    private fun anApplicantWithGoodCredit() = dev.kensa.Action<dev.kensa.GivensContext> {
        applicant = Applicant("Alice", creditScore = 750, amount = 10_000)
    }

    private fun theLoanServiceProcessesTheApplication() = dev.kensa.Action<dev.kensa.ActionContext> {
        result = service.process(applicant)
    }

    private fun theLoanResult() = StateCollector { result }

    private fun theLoanReference() = StateCollector { result.reference }
}

class HamkrestBridgeSnippets : KensaTest, WithHamkrest {

    private val service = LoanService()
    private lateinit var applicant: Applicant
    private lateinit var result: LoanResult

    @Test
    fun `loan is approved`() {
        given(anApplicantWithGoodCredit())
        whenever(theLoanServiceProcessesTheApplication())
        then(theLoanStatus(), equalTo(LoanStatus.Approved))
        and(theLoanResult()) { assertThat(reference, startsWith("LN-")) }
    }

    @Test
    fun `loan is eventually approved`() {
        given(anApplicantWithGoodCredit())
        whenever(theLoanServiceProcessesTheApplication())
        thenEventually(theLoanStatus(), equalTo(LoanStatus.Approved))
        thenContinually(2.seconds, theLoanReference(), startsWith("LN-"))
    }

    private fun anApplicantWithGoodCredit() = dev.kensa.Action<dev.kensa.GivensContext> {
        applicant = Applicant("Alice", creditScore = 750, amount = 10_000)
    }

    private fun theLoanServiceProcessesTheApplication() = dev.kensa.Action<dev.kensa.ActionContext> {
        result = service.process(applicant)
    }

    private fun theLoanResult() = StateCollector { result }

    private fun theLoanStatus() = StateCollector { result.status }

    private fun theLoanReference() = StateCollector { result.reference }
}

class HamcrestBridgeSnippets : KensaTest, WithHamcrest {

    private val service = LoanService()
    private lateinit var applicant: Applicant
    private lateinit var result: LoanResult

    @Test
    fun `loan is approved`() {
        given(anApplicantWithGoodCredit())
        whenever(theLoanServiceProcessesTheApplication())
        then(theLoanStatus(), `is`(LoanStatus.Approved))
        and(theLoanReference(), org.hamcrest.Matchers.startsWith("LN-"))
    }

    private fun anApplicantWithGoodCredit() = dev.kensa.Action<dev.kensa.GivensContext> {
        applicant = Applicant("Alice", creditScore = 750, amount = 10_000)
    }

    private fun theLoanServiceProcessesTheApplication() = dev.kensa.Action<dev.kensa.ActionContext> {
        result = service.process(applicant)
    }

    private fun theLoanStatus() = StateCollector { result.status }

    private fun theLoanReference() = StateCollector { result.reference }
}

class AssertJBridgeSnippets : KensaTest, WithAssertJ {

    private val service = LoanService()
    private lateinit var applicant: Applicant
    private lateinit var result: LoanResult

    @Test
    fun `loan is approved`() {
        given(anApplicantWithGoodCredit())
        whenever(theLoanServiceProcessesTheApplication())
        then(theLoanResult()) { assertThat(it.status).isEqualTo(LoanStatus.Approved) }
        and(theLoanReference()).startsWith("LN-")
    }

    private fun anApplicantWithGoodCredit() = dev.kensa.Action<dev.kensa.GivensContext> {
        applicant = Applicant("Alice", creditScore = 750, amount = 10_000)
    }

    private fun theLoanServiceProcessesTheApplication() = dev.kensa.Action<dev.kensa.ActionContext> {
        result = service.process(applicant)
    }

    private fun theLoanResult() = StateCollector { result }

    private fun theLoanReference() = StringStateCollector { result.reference }
}
