package dev.kensa.example;

import dev.kensa.*;
import dev.kensa.fixture.MyScenario;
import dev.kensa.hamcrest.WithHamcrest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;

public class JavaWithExpandableSentenceTest extends JavaExampleTest implements WithHamcrest {

    @RenderedValue
    private final String aValue = "aStringValue";

    @RenderedValue
    private final MyScenario myScenario = new MyScenario(aValue);

    @RenderedValue
    private MyScenario myScenario2 = new MyScenario("Meh");

    @Test
    void passingTest() {
        given(somePrerequisites());

        whenever(someActionWith("someParameterValue"));

        then(theExtractedValue(), is(aValue));
    }

    @Test
    void testWithExpandableScenarioParameter() {
        given(somePrerequisites());

        whenever(someAction(myScenario));

        whenever(someAction(myScenario2));
    }

    @Test
    void testWithExpandableSentenceExpression() {
        givenSomePrerequisites();

        wheneverSomeAction(myScenario);

        whenever(someAction(myScenario2));
    }

    @NotNull
    private StateCollector<String> theExtractedValue() {
        return context -> aValue;
    }

    @ExpandableSentence
    private void givenSomePrerequisites() {
         given((Action<GivensContext>) (context) -> {});
    }

    @ExpandableSentence
    private Action<GivensContext> somePrerequisites() {
        return (context) -> {};
    }

    @ExpandableSentence
    private Action<ActionContext> wheneverSomeAction(@RenderedValue MyScenario aScenarioOf) {
        return someActionUnderTest(aScenarioOf.getStringValue());
    }

    @ExpandableSentence
    private Action<ActionContext> someActionWith(@RenderedValue String parameter1) {
        return someActionUnderTest(parameter1);
    }

    @ExpandableSentence
    private Action<ActionContext> someAction(@RenderedValue MyScenario aScenarioOf) {
        return someActionUnderTest(aScenarioOf.getStringValue());
    }

    @NotNull
    private static Action<ActionContext> someActionUnderTest(String withAParam) {
        return (context) -> {
        };
    }
}
