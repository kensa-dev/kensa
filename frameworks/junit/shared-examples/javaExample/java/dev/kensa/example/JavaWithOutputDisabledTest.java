package dev.kensa.example;

import dev.kensa.Action;
import dev.kensa.ActionContext;
import dev.kensa.GivensContext;
import dev.kensa.RenderedValue;
import dev.kensa.StateCollector;
import dev.kensa.hamcrest.WithHamcrest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;

public class JavaWithOutputDisabledTest extends JavaExampleTest implements WithHamcrest {

    @RenderedValue
    private final String aValue = "aStringValue";

    @Test
    void passingTest() {
        given(somePrerequisites());

        whenever(someAction());

        then(theExtractedValue(), is(aValue));
    }

    @NotNull
    private StateCollector<String> theExtractedValue() {
        return context -> aValue;
    }

    private Action<GivensContext> somePrerequisites() {
        return context -> {
        };
    }

    private Action<ActionContext> someAction() {
        return context -> {
        };
    }
}
