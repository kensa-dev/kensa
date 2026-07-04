// Snippet source for kensa.dev/docs/api/async-assertions.md — Java/AssertJ tab
package apidocs;

import dev.kensa.StateCollector;
import dev.kensa.assertj.WithAssertJ;
import dev.kensa.junit.KensaTest;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncAssertionsJavaSnippets implements KensaTest, WithAssertJ {

    static class OrderService {
        void placeOrder() {}
        String status() { return "CONFIRMED"; }
    }

    private final OrderService orderService = new OrderService();

    @Test
    void orderIsEventuallyConfirmed() {
        whenever(ctx -> orderService.placeOrder());

        // polls the collector until the assertion passes (default: 10 seconds)
        thenEventually(theOrderStatus(), status -> assertThat(status).isEqualTo("CONFIRMED"));
    }

    @Test
    void orderIsEventuallyConfirmedWithCustomTimeout() {
        whenever(ctx -> orderService.placeOrder());

        thenEventually(30L, ChronoUnit.SECONDS, theOrderStatus(),
                status -> assertThat(status).isEqualTo("CONFIRMED"));
    }

    private StateCollector<String> theOrderStatus() {
        return ctx -> orderService.status();
    }
}
