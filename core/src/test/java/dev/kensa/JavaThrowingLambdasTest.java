package dev.kensa;

import dev.kensa.attachments.Attachments;
import dev.kensa.context.TestContext;
import dev.kensa.fixture.Fixtures;
import dev.kensa.outputs.CapturedOutputs;
import dev.kensa.state.CapturedInteractions;
import dev.kensa.state.SetupStrategy;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.kotest.matchers.ShouldKt.shouldBe;
import static io.kotest.matchers.types.MatchersKt.shouldBeSameInstanceAs;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaThrowingLambdasTest {

    @Nested
    class ActionOf {

        private final GivensContext givensContext = new GivensContext(new Fixtures(), new CapturedOutputs());

        @Test
        void propagatesTheCheckedExceptionUnwrapped() {
            IOException failure = new IOException("journal unavailable");
            Action<GivensContext> action = Action.of(context -> {
                throw failure;
            });

            shouldBeSameInstanceAs(assertThrows(IOException.class, () -> action.execute(givensContext)), failure);
        }
    }

    @Nested
    class StateCollectorOf {

        private final CollectorContext collectorContext = new CollectorContext(new Fixtures(), new CapturedInteractions(SetupStrategy.Ungrouped), new CapturedOutputs());

        @Test
        void returnsTheCollectedValue() {
            StateCollector<String> collector = StateCollector.of(context -> readReference());

            shouldBe(collector.execute(collectorContext), "REF-1");
        }

        @Test
        void propagatesTheCheckedExceptionUnwrapped() {
            IOException failure = new IOException("queue unavailable");
            StateCollector<String> collector = StateCollector.of(context -> {
                throw failure;
            });

            shouldBeSameInstanceAs(assertThrows(IOException.class, () -> collector.execute(collectorContext)), failure);
        }

        private String readReference() throws IOException {
            return "REF-1";
        }
    }

    @Nested
    class SetupStepOf {

        private final TestContext testContext = new TestContext(new CapturedInteractions(SetupStrategy.Ungrouped), new Fixtures(), new CapturedOutputs(), new Attachments());

        @Test
        void runsEachActionInOrderWithTheActionContext() {
            List<String> sent = new ArrayList<>();

            testContext.given(new SetupSteps(SetupStep.of(
                    context -> sent.add(sendMessage(context, "open account")),
                    context -> sent.add(sendMessage(context, "fund account"))
            )));

            shouldBe(sent, List.of("open account", "fund account"));
            shouldBe(testContext.getInteractions().containsKey("fund account"), true);
        }

        @Test
        void propagatesTheCheckedExceptionUnwrapped() {
            IOException failure = new IOException("broker unavailable");
            SetupStep step = SetupStep.of(context -> {
                throw failure;
            });

            shouldBeSameInstanceAs(assertThrows(IOException.class, () -> testContext.given(new SetupSteps(step))), failure);
        }

        private String sendMessage(ActionContext context, String message) throws IOException {
            context.getInteractions().put(message, message);
            return message;
        }
    }
}
