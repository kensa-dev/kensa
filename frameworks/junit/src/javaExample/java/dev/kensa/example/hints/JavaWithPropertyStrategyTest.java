package dev.kensa.example.hints;

import dev.kensa.RenderedValueWithHint;
import dev.kensa.example.JavaExampleTest;
import org.junit.jupiter.api.Test;

import static dev.kensa.RenderedHintStrategy.HintFromProperty;
import static dev.kensa.RenderedValueStrategy.UseProperty;

@RenderedValueWithHint(type = DataField.class, valueStrategy = UseProperty, valueParam = "label", hintStrategy = HintFromProperty, hintParam = "xpath")
public class JavaWithPropertyStrategyTest extends JavaExampleTest {

    private final DataField accountBalance = new DataField("Account Balance", "//div[@id='balance']");

    @Test
    void passingTest() {
        accountBalance.of("£100.00");
    }
}

record DataField(String label, String xpath) {
    public DataField of(String value) {
        return this;
    }
}