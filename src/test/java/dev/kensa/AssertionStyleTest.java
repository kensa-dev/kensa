package dev.kensa;

import dev.kensa.sentence.Acronym;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;

class AssertionStyleTest implements KensaTest {

    private final String actionName = "ACTION1";

    @SentenceValue
    private final String theExpectedResult = "Performed: ACTION1";

    private final ActionPerformer performer = new ActionPerformer();

    @BeforeEach
    void setUp() {
        Kensa.configure()
             .withAcronyms(Acronym.of("ACTION1", ""))
             .withKeywords("that");
    }

    @Test
    void canUseAssertJStyle() {
        given(someActionNameIsAddedToGivens());

        when(theActionIsPerformedAndTheResultIsAddedToCapturedInteractions());

        then(theResultStoredInCapturedInteractions()).isEqualTo(theExpectedResult);
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
            String actionName = givens.get("actionName", String.class);
            capturedInteractions.put("result", performer.perform(actionName));
        };
    }

    private StateExtractor<String> theResultStoredInCapturedInteractions() {
        return capturedInteractions -> capturedInteractions.get("result", String.class);
    }

    private static Stream<Arguments> parameterProvider() {
        return Stream.of(
                Arguments.arguments("ACTION2", "Performed: ACTION2"),
                Arguments.arguments("ACTION3", "Performed: ACTION3")
        );
    }
}
