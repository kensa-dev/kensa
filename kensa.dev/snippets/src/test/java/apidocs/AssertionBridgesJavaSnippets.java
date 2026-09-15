// Snippet source for kensa.dev/docs/api/assertion-bridges.md — Java examples
package apidocs;

import dev.kensa.Action;
import dev.kensa.ActionContext;
import dev.kensa.GivensContext;
import dev.kensa.StateCollector;
import dev.kensa.assertj.StringStateCollector;
import dev.kensa.assertj.WithAssertJ;
import dev.kensa.hamcrest.WithHamcrest;
import dev.kensa.junit.KensaTest;
import org.junit.jupiter.api.Test;
import quickstart.Applicant;
import quickstart.LoanResult;
import quickstart.LoanService;
import quickstart.LoanStatus;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

class AssertionBridgesJavaSnippets {

    static class AssertJBridge implements KensaTest, WithAssertJ {

        private final LoanService service = new LoanService();
        private Applicant applicant;
        private LoanResult result;

        @Test
        void loanIsApproved() {
            given(anApplicantWithGoodCredit());
            whenever(theLoanServiceProcessesTheApplication());
            then(theLoanResult(), r -> assertThat(r.getStatus()).isEqualTo(LoanStatus.Approved));
            and(theLoanReference()).startsWith("LN-");
        }

        @Test
        void loanIsEventuallyApproved() {
            given(anApplicantWithGoodCredit());
            whenever(theLoanServiceProcessesTheApplication());
            thenEventually(theLoanResult(), r -> assertThat(r.getStatus()).isEqualTo(LoanStatus.Approved));
            thenEventually(30L, ChronoUnit.SECONDS, theLoanReference(), ref -> assertThat(ref).startsWith("LN-"));
        }

        private Action<GivensContext> anApplicantWithGoodCredit() {
            return ctx -> applicant = new Applicant("Alice", 750, 10_000);
        }

        private Action<ActionContext> theLoanServiceProcessesTheApplication() {
            return ctx -> result = service.process(applicant);
        }

        private StateCollector<LoanResult> theLoanResult() {
            return ctx -> result;
        }

        private StringStateCollector theLoanReference() {
            return ctx -> result.getReference();
        }
    }

    static class HamcrestBridge implements KensaTest, WithHamcrest {

        private final LoanService service = new LoanService();
        private Applicant applicant;
        private LoanResult result;

        @Test
        void loanIsApproved() {
            given(anApplicantWithGoodCredit());
            whenever(theLoanServiceProcessesTheApplication());
            then(theLoanStatus(), is(LoanStatus.Approved));
            and(theLoanReference(), startsWith("LN-"));
        }

        @Test
        void loanIsEventuallyApproved() {
            given(anApplicantWithGoodCredit());
            whenever(theLoanServiceProcessesTheApplication());
            thenEventually(theLoanStatus(), is(LoanStatus.Approved));
            thenContinually(Duration.ofSeconds(2), theLoanReference(), startsWith("LN-"));
        }

        private Action<GivensContext> anApplicantWithGoodCredit() {
            return ctx -> applicant = new Applicant("Alice", 750, 10_000);
        }

        private Action<ActionContext> theLoanServiceProcessesTheApplication() {
            return ctx -> result = service.process(applicant);
        }

        private StateCollector<LoanStatus> theLoanStatus() {
            return ctx -> result.getStatus();
        }

        private StateCollector<String> theLoanReference() {
            return ctx -> result.getReference();
        }
    }
}
