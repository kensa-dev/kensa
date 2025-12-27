package dev.kensa.hints;

import dev.kensa.RenderedValueWithHint;
import dev.kensa.junit.KensaTest;
import org.junit.jupiter.api.Test;

import static dev.kensa.RenderedHintStrategy.HintFromMethod;
import static dev.kensa.RenderedValueStrategy.UseMethod;

@RenderedValueWithHint(type = MethodField.class, valueStrategy = UseMethod, valueParam = "displayName", hintStrategy = HintFromMethod, hintParam = "technicalPath")
public class JavaWithMethodHintStrategyTest implements KensaTest {

    private final MethodField userEmail = new MethodField("User Email", "/api/v1/user/email");

    @Test
    void passingTest() {
        userEmail.of("test@example.com");
    }
}

class MethodField {
    private final String name;
    private final String path;

    public MethodField(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String displayName() { return name; }
    public String technicalPath() { return path; }
    public MethodField of(String value) { return this; }
}