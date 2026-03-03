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
    private StateExtractor<String> theExtractedValue() {
        return interactions -> aValue;
    }

    @ExpandableSentence
    private void givenSomePrerequisites() {
         given((GivensBuilder) (givens) -> givens.put("foo", "bar"));
    }

    @ExpandableSentence
    private GivensBuilder somePrerequisites() {
        return (givens) -> givens.put("foo", "bar");
    }

    @ExpandableSentence
    private ActionUnderTest wheneverSomeAction(@RenderedValue MyScenario aScenarioOf) {
        return someActionUnderTest(aScenarioOf.getStringValue());
    }

    @ExpandableSentence
    private ActionUnderTest someActionWith(@RenderedValue String parameter1) {
        return someActionUnderTest(parameter1);
    }

    @ExpandableSentence
    private ActionUnderTest someAction(@RenderedValue MyScenario aScenarioOf) {
        return someActionUnderTest(aScenarioOf.getStringValue());
    }

    @NotNull
    private static ActionUnderTest someActionUnderTest(String withAParam) {
        return (givens, interactions) -> {
        };
    }
}
