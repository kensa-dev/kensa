package dev.kensa.acceptance.example;

import dev.kensa.Highlight;
import dev.kensa.Scenario;
import dev.kensa.SentenceValue;
import dev.kensa.java.JavaKensaTest;
import dev.kensa.java.WithAssertJ;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public interface JavaTestInterface extends JavaKensaTest, WithAssertJ {

    String field4 = "4";

    @Scenario
    String field5 = "5";

    @Highlight
    @SentenceValue
    String field6 = "6";

    @Test
    default void interfaceTestMethod() {
        assertThat("abc").contains("a");
    }
}
