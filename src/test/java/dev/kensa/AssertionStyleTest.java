package dev.kensa;

import dev.kensa.java.JavaKensaTest;
import dev.kensa.java.WithAssertJ;
import dev.kensa.java.WithHamcrest;
import dev.kensa.sentence.Acronym;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;

class AssertionStyleTest implements JavaKensaTest, WithHamcrest, WithAssertJ {

    private final String actionName = "ACTION1";

    @SentenceValue
    private final String theExpectedResult = "Performed: ACTION1";

    @Scenario
    private final ScenarioFoo scenario = new ScenarioFoo();

    private final ActionPerformer performer = new ActionPerformer();

    @BeforeEach
    void setUp() {
        Kensa.configure()
             .withAcronyms(Acronym.of("ACTION1", ""))
             .withKeywords("that");
    }

    @Test
    void canUseAssertJStyle() {
        given(someActionNameIsAddedToGivens()); // Comment

        when(theActionIsPerformedAndTheResultIsAddedToCapturedInteractions());

        then(theResultStoredInCapturedInteractions()).isEqualTo(theExpectedResult);
        withAllTheNestedThings();

        then(foo1())
                .isEqualTo("777");
        then(foo())
                .isEqualTo(666);
    }

    @NotNull
    private StateExtractor<Integer> foo() {
        return interactions -> 666;
    }

    @NotNull
    private StateExtractor<String> foo1() {
        return interactions -> "777";
    }

    @NestedSentence
    private void withAllTheNestedThings() {
        then(foo()).isEqualTo(scenario.thing());
    }

    @Test
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

    public static class ScenarioFoo {
        public Integer thing() {
            return 666;
        }
    }
}
