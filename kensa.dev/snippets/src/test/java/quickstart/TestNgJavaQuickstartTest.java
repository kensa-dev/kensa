// Snippet source for kensa.dev/docs/quickstart/testng-quickstart.md §3 Java tab + §5 listener
package quickstart;

import dev.kensa.Action;
import dev.kensa.ActionContext;
import dev.kensa.GivensContext;
import dev.kensa.RenderedValue;
import dev.kensa.StateCollector;
import dev.kensa.assertj.WithAssertJ;
import dev.kensa.testng.KensaTest;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestNgJavaQuickstartTest implements KensaTest, WithAssertJ {

    @RenderedValue
    private final String applicantName = "Alice";

    @RenderedValue
    private final int requestedAmount = 10_000;

    private final LoanService service = new LoanService();
    private Applicant applicant;
    private LoanResult result;

    @Test
    void canApproveLoanForApplicantWithGoodCredit() {
        given(anApplicantWithGoodCredit());
        whenever(theLoanServiceProcessesTheApplication());
        then(theLoanResult(), r -> assertThat(r.getStatus()).isEqualTo(LoanStatus.Approved));
    }

    @Test
    void canDeclineLoanForApplicantWithPoorCredit() {
        given(anApplicantWithPoorCredit());
        whenever(theLoanServiceProcessesTheApplication());
        then(theLoanResult(), r -> assertThat(r.getStatus()).isEqualTo(LoanStatus.Declined));
    }

    // --- Givens ---

    private Action<GivensContext> anApplicantWithGoodCredit() {
        return ctx -> applicant = new Applicant(applicantName, 750, requestedAmount);
    }

    private Action<GivensContext> anApplicantWithPoorCredit() {
        return ctx -> applicant = new Applicant(applicantName, 300, requestedAmount);
    }

    // --- Action ---

    private Action<ActionContext> theLoanServiceProcessesTheApplication() {
        return ctx -> result = service.process(applicant);
    }

    // --- State ---

    private StateCollector<LoanResult> theLoanResult() {
        return ctx -> result;
    }
}
