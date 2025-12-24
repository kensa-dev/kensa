package dev.kensa.hints;

import dev.kensa.RenderedValueWithHint;
import dev.kensa.junit.KensaTest;
import org.junit.jupiter.api.Test;

import static dev.kensa.RenderedHintStrategy.HintFromProperty;
import static dev.kensa.RenderedValueStrategy.UseIdentifierName;

@RenderedValueWithHint(type = JavaFieldWithPathHint.class, valueStrategy = UseIdentifierName, hintStrategy = HintFromProperty, hintParam = "path")
public class JavaWithHintedFieldsInsideTest implements KensaTest {

    private final JavaFieldWithPathHint<String> aString = new JavaFieldWithPathHint<>("/path/To/String/Field");
    private final JavaFieldWithPathHint<Integer> anInteger = new JavaFieldWithPathHint<>("/path/To/Integer/Field");

    @Test
    void passingTest() {
        aString.of("expected");
        anInteger.of(10);
    }
}