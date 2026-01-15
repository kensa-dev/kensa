package dev.kensa.example.hints;

import dev.kensa.RenderedValueWithHint;
import dev.kensa.Sources;
import dev.kensa.example.JavaExampleTest;
import org.junit.jupiter.api.Test;

import static dev.kensa.RenderedHintStrategy.HintFromProperty;
import static dev.kensa.RenderedValueStrategy.UseIdentifierName;
import static dev.kensa.example.hints.JavaFields.aString;
import static dev.kensa.example.hints.JavaFields.anInteger;

@RenderedValueWithHint(type = JavaFieldWithPathHint.class, valueStrategy = UseIdentifierName, hintStrategy = HintFromProperty, hintParam = "path")
@Sources({JavaFields.class})
public class JavaWithHintedFieldsInsideObjectTest extends JavaExampleTest {

    @Test
    void passingTest() {
        aString.of("expected");
        anInteger.of(10);
    }
}