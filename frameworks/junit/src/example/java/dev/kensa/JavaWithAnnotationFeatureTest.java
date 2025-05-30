package dev.kensa;

import dev.kensa.fixture.MyScenario;
import dev.kensa.hamcrest.WithHamcrest;
import dev.kensa.junit.KensaTest;
import org.junit.jupiter.api.Test;

import static dev.kensa.Colour.BackgroundDanger;
import static dev.kensa.Colour.TextLight;
import static dev.kensa.TextStyle.*;
import static org.hamcrest.CoreMatchers.is;

class JavaWithAnnotationFeatureTest implements KensaTest, WithHamcrest {

    @Highlight
    private String highlightMe = "givensViaHighlight";

    @SentenceValue
    private String aValue = "aStringValue";

    @Scenario
    private MyScenario myScenario = new MyScenario(aValue);

    @Test
    void testWithScenario() {
        given(somePrerequisites());

        whenever(someAction());

        then(theExtractedValue(), is(myScenario.getStringValue()));
    }

    @Test
    void testWithHighlight() {
        given(somePrerequisitesWith("noHighlight"));

        whenever(someActionWith(highlightMe));

        then(theExtractedValue(), is(aValue));
    }

    @Test
    void testWithEmphasis() {
        whenever(someActionWithEmphasis());

        then(theExtractedValue(), is(aValue));
    }

    private StateExtractor<String> theExtractedValue() {
        return interactions -> aValue;
    }

    private GivensBuilder somePrerequisites() {
        return (givens, fixtures) -> givens.put("foo", "bar");
    }

    private GivensBuilder somePrerequisitesWith(String... values) {
        return (givens, fixtures) -> {
            for (String value : values) {
                givens.put("key_" + value, value);
            }
        };
    }

    private ActionUnderTest someAction() {
        return (givens, fixtures, interactions) -> {
        };
    }

    private ActionUnderTest someActionWith(String value) {
        return (givens, fixtures,interactions) -> {
        };
    }

    @Emphasise(textStyles = {TextWeightBold, Italic, Uppercase}, textColour = TextLight, backgroundColor = BackgroundDanger)
    private ActionUnderTest someActionWithEmphasis() {
        return (givens, fixtures, interactions) -> {
        };
    }
}
