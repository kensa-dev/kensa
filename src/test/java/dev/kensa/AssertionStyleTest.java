package dev.kensa;

import dev.kensa.acceptance.example.JavaTestInterface;
import dev.kensa.java.JavaKensaTest;
import dev.kensa.java.WithAssertJ;
import dev.kensa.java.WithHamcrest;
import dev.kensa.sentence.Acronym;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static dev.kensa.Colour.BackgroundDanger;
import static dev.kensa.Colour.TextLight;
import static dev.kensa.TextStyle.Italic;
import static dev.kensa.TextStyle.TextWeightBold;
import static dev.kensa.TextStyle.Uppercase;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.hamcrest.CoreMatchers.is;

@Notes("Some notes {@link dev.kensa.AssertionStyleTest#canUseAssertJStyle} with text in between " +
        "{@link dev.kensa.AssertionStyleTest#canUseHamcrestStyle} and some trailing text")
class AssertionStyleTest implements JavaKensaTest, WithHamcrest, WithAssertJ, JavaTestInterface {

    private final String actionName = "ACTION1";

    @SentenceValue
    private final String theExpectedResult = "Performed: ACTION1";

    @Scenario
    private final ScenarioFoo scenario = new ScenarioFoo();

    private final ActionPerformer performer = new ActionPerformer();

    @BeforeEach
    void setUp() {
        Kensa.configure()
             .withAcronyms(Acronym.of("ACTION1", ""), Acronym.of("AB", "A short Acronym"))
             .withKeywords("that");
    }

    @Test
    @Notes("Some notes {@link dev.kensa.AssertionStyleTest#canUseAssertJStyle} and some more")
    void canUseAssertJStyle() {
        given(someActionNameIsAddedToGivens()); // Comment

        when(theActionIsPerformedAndTheResultIsAddedToCapturedInteractions());

        then(theResultStoredInCapturedInteractions()).isEqualTo(theExpectedResult);
        withAllTheNestedThings();

        then(foo1())
                .isEqualTo("777")
                    .hasSameClassAs("888");
        then(foo())
                .isEqualTo(666);
    }

    private StateExtractor<Integer> foo() {
        return interactions -> 666;
    }

    private StateExtractor<String> foo1() {
        return interactions -> "777";
    }

    @NestedSentence
    private void withAllTheNestedThings() {
        then(foo()).isEqualTo(scenario.thing());
    }

    @Test
    @Notes("No link here")
    void canUseHamcrestStyle() {
        given(someActionNameIsAddedToGivens());

        when(theActionIsPerformedAndTheResultIsAddedToCapturedInteractions());

        then(theResultStoredInCapturedInteractions());
    }

    @ParameterizedTest
    @MethodSource("parameterProvider")
    void parameterizedTest(@SentenceValue String actionName, @SentenceValue String theExpectedResult) {
        given(somethingIsDoneWith(actionName));

        when(theActionIsPerformedAndTheResultIsAddedToCapturedInteractions());

        then(theResultStoredInCapturedInteractions(), is(theExpectedResult));
    }

    private GivensBuilder somethingIsDoneWith(String actionName) {
        return (givens) -> givens.put("actionName", actionName);
    }

    private GivensBuilder someActionNameIsAddedToGivens() {
        return (givens) -> givens.put("actionName", actionName);
    }

    @Emphasise(textStyles = {TextWeightBold, Italic, Uppercase}, textColour = TextLight, backgroundColor = BackgroundDanger)
    private ActionUnderTest theActionIsPerformedAndTheResultIsAddedToCapturedInteractions() {
        return (givens, capturedInteractions) -> {
            String actionName = givens.get("actionName");
            capturedInteractions.put("result", performer.perform(actionName));
        };
    }

    private StateExtractor<String> theResultStoredInCapturedInteractions() {
        return capturedInteractions -> capturedInteractions.get("result");
    }

    private static Stream<Arguments> parameterProvider() {
        return Stream.of(
                Arguments.arguments("ACTION2", "Performed: ACTION2"),
                Arguments.arguments("ACTION3", "Performed: ACTION3")
        );
    }

    @Test
    void canUseThenEventuallyToWaitForResult() {
        when(aResultIsAddedToCapturedInteractionsAfterSomeTime());

        thenEventually(theResultStoredInCapturedInteractions(), is("result added after some time"));
    }

    private ActionUnderTest aResultIsAddedToCapturedInteractionsAfterSomeTime() {
        return (givens, capturedInteractions) -> newSingleThreadScheduledExecutor()
                .schedule(() -> {
                    capturedInteractions.put("result", "result added after some time");
                }, 300, TimeUnit.MILLISECONDS);
    }

    public static class ScenarioFoo {
        public Integer thing() {
            return 666;
        }
    }
}
