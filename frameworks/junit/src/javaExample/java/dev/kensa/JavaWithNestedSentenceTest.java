package dev.kensa;

import dev.kensa.fixture.MyScenario;
import dev.kensa.hamcrest.WithHamcrest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;

public class JavaWithNestedSentenceTest extends JavaExampleTest implements WithHamcrest {

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
    void testWithNestedScenarioParameter() {
        given(somePrerequisites());

        whenever(someAction(myScenario));

        whenever(someAction(myScenario2));
    }

    @Test
    void testWithNestedSentenceExpression() {
        givenSomePrerequisites();

        wheneverSomeAction(myScenario);

        whenever(someAction(myScenario2));
    }

    @NotNull
    private StateExtractor<String> theExtractedValue() {
        return interactions -> aValue;
    }

    @NestedSentence
    private void givenSomePrerequisites() {
         given((GivensBuilder) (givens) -> givens.put("foo", "bar"));
    }

    @NestedSentence
    private GivensBuilder somePrerequisites() {
        return (givens) -> givens.put("foo", "bar");
    }

    @NestedSentence
    private ActionUnderTest wheneverSomeAction(@RenderedValue MyScenario aScenarioOf) {
        return someActionUnderTest(aScenarioOf.getStringValue());
    }

    @NestedSentence
    private ActionUnderTest someActionWith(@RenderedValue String parameter1) {
        return someActionUnderTest(parameter1);
    }

    @NestedSentence
    private ActionUnderTest someAction(@RenderedValue MyScenario aScenarioOf) {
        return someActionUnderTest(aScenarioOf.getStringValue());
    }

    @NotNull
    private static ActionUnderTest someActionUnderTest(String withAParam) {
        return (givens, interactions) -> {
        };
    }
}
