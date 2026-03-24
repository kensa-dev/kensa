package dev.kensa.example;

import dev.kensa.*;
import dev.kensa.fixture.MyScenario;
import dev.kensa.hamcrest.WithHamcrest;
import org.junit.jupiter.api.Test;

import static dev.kensa.Colour.BackgroundDanger;
import static dev.kensa.Colour.TextLight;
import static dev.kensa.TextStyle.*;
import static org.hamcrest.CoreMatchers.is;

public class JavaWithAnnotationFeatureTest extends JavaExampleTest implements WithHamcrest {

    @Highlight
    private String highlightMe = "givensViaHighlight";

    @RenderedValue
    private String aValue = "aStringValue";

    @RenderedValue
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

    @Test
    void testWithRenderedValueFunctionWithParameters() {
        given(somePrerequisitesWith(renderTheReturnValue("foo", "bar")));
    }

    @RenderedValue
    private String renderTheReturnValue(String one, String two) {
        return String.format("%s-%s", one, two);
    }

    private StateCollector<String> theExtractedValue() {
        return context -> aValue;
    }

    private Action<GivensContext> somePrerequisites() {
        return (context) -> {
        };
    }

    private Action<GivensContext> somePrerequisitesWith(String... values) {
        return (context) -> {
        };
    }

    private Action<ActionContext> someAction() {
        return (context) -> {
        };
    }

    private Action<ActionContext> someActionWith(String value) {
        return (context) -> {
        };
    }

    @Emphasise(textStyles = {TextWeightBold, Italic, Uppercase}, textColour = TextLight, backgroundColor = BackgroundDanger)
    private Action<ActionContext> someActionWithEmphasis() {
        return (context) -> {
        };
    }
}
