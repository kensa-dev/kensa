package dev.kensa.example;

import dev.kensa.ActionUnderTest;
import dev.kensa.GivensBuilder;
import dev.kensa.RenderedValue;
import dev.kensa.StateExtractor;
import dev.kensa.hamcrest.WithHamcrest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;

public class JavaWithSinglePassingTest extends JavaExampleTest implements WithHamcrest {

    @RenderedValue
    private final String aValue = "aStringValue";

    @Test
    void passingTest() {
        given(somePrerequisites());

        whenever(someAction());

        then(theExtractedValue(), is(aValue));
    }

    @NotNull
    private StateExtractor<String> theExtractedValue() {
        return interactions -> aValue;
    }

    private GivensBuilder somePrerequisites() {
        return (givens) -> givens.put("foo", "bar");
    }

    private ActionUnderTest someAction() {
        return (givens, interactions) -> {
        };
    }
}
