package dev.kensa.outputs;

import dev.kensa.util.NamedValue;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.kotest.matchers.ShouldKt.shouldBe;
import static io.kotest.matchers.types.MatchersKt.shouldBeSameInstanceAs;

class JavaHighlightTest {

    private final CapturedOutputs outputs = new CapturedOutputs();

    @Test
    void highlightRecordsAHighlightedValueAndReturnsIt() {
        String reference = "REF-1";

        shouldBeSameInstanceAs(outputs.highlight("Order reference", reference), reference);
        shouldBe(outputs.highlightedValues(), Set.of(new NamedValue("Order reference", "REF-1")));
    }

    @Test
    void highlightSkipsNullSilently() {
        shouldBe(outputs.highlight("Order reference", null), null);
        shouldBe(outputs.values(), Set.of());
    }
}
