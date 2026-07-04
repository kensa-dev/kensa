// Snippet source for kensa.dev/docs/api/outputs.md + attachments.md — Java tabs
package apidocs;

import dev.kensa.assertj.WithAssertJ;
import dev.kensa.attachments.TypedKey;
import dev.kensa.junit.KensaTest;
import dev.kensa.outputs.CapturedOutput;
import org.junit.jupiter.api.Test;

import static dev.kensa.context.TestContextHolder.testContext;
import static dev.kensa.outputs.CapturedOutputsKt.createCapturedOutput;
import static org.assertj.core.api.Assertions.assertThat;

class OutputsAttachmentsSnippets implements KensaTest, WithAssertJ {

    static class HttpResponse {
        int statusCode() { return 200; }
    }

    static class MySnapshot {}

    static final CapturedOutput<HttpResponse> RESPONSE =
            createCapturedOutput("response", HttpResponse.class);

    static final CapturedOutput<String> TRANSACTION_ID =
            createCapturedOutput("transaction id", String.class, true);

    static final TypedKey<MySnapshot> MY_SNAPSHOT = new TypedKey<>("my-snapshot");

    private HttpResponse submit() { return new HttpResponse(); }

    private MySnapshot capture() { return new MySnapshot(); }

    @Test
    void paymentIsAccepted() {
        given(ctx -> { /* ... */ });
        whenever(ctx -> ctx.getOutputs().set(RESPONSE, submit()));
        then(ctx -> ctx.getOutputs().get(RESPONSE),
                response -> assertThat(response.statusCode()).isEqualTo(200));
    }

    @Test
    void stringKeys() {
        whenever(ctx -> ctx.getOutputs().put("response", submit()));
        then(ctx -> (HttpResponse) ctx.getOutputs().get("response"),
                response -> assertThat(response.statusCode()).isEqualTo(200));
    }

    @Test
    void highlighted() {
        whenever(ctx -> ctx.getOutputs().set(TRANSACTION_ID, "TX-1"));
    }

    @Test
    void somethingHappens() {
        whenever(ctx -> {
            MySnapshot snapshot = capture();
            testContext().getAttachments().put(MY_SNAPSHOT, snapshot);
        });
    }
}
