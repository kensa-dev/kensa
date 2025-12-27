package dev.kensa.hints;

import dev.kensa.RenderedValueWithHint;
import dev.kensa.junit.KensaTest;
import org.junit.jupiter.api.Test;

import static dev.kensa.RenderedHintStrategy.HintFromMethod;
import static dev.kensa.RenderedValueStrategy.UseToString;

@RenderedValueWithHint(type = ToStringField.class, valueStrategy = UseToString, hintStrategy = HintFromMethod, hintParam = "getMetadata")
public class JavaWithMixedStrategyTest implements KensaTest {

    private final ToStringField transactionId = new ToStringField("ID", "99");

    @Test
    void passingTest() {
        transactionId.of("TXN-99");
    }
}

class ToStringField {
    private final String prefix;
    private final String suffix;

    public ToStringField(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String getMetadata() { return "SystemPrefix: " + prefix; }

    @Override
    public String toString() { return prefix + "-" + suffix; }

    public ToStringField of(String value) { return this; }
}