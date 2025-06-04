package dev.kensa;

import dev.kensa.hamcrest.WithHamcrest;
import dev.kensa.junit.KensaTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;

public class JavaWithNestedSentenceTest implements KensaTest, WithHamcrest {

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
        return (givens, fixtures) -> givens.put("foo", "bar");
    }

    @NestedSentence
    private ActionUnderTest someAction() {
        return someActionUnderTest();
    }

    @NotNull
    private static ActionUnderTest someActionUnderTest() {
        return (givens, fixtures, interactions) -> {
        };
    }
}
