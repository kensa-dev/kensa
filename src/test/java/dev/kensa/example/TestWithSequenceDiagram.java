package dev.kensa.example;

import dev.kensa.ActionUnderTest;
import dev.kensa.GivensBuilder;
import dev.kensa.java.JavaKensaTest;
import dev.kensa.state.Party;
import org.junit.jupiter.api.Test;

import static dev.kensa.example.TestWithSequenceDiagram.TestParty.Kiki;
import static dev.kensa.example.TestWithSequenceDiagram.TestParty.Openreach;
import static dev.kensa.state.CapturedInteractionBuilder.from;

class TestWithSequenceDiagram implements JavaKensaTest {

    @Test
    void foo() {
        given(someThingIsDone());

        when(someThingHappens());
    }


    private GivensBuilder someThingIsDone() {
        return givens -> {};
    }

    private ActionUnderTest someThingHappens() {
        return (givens, interactions) -> {
            interactions.capture(from(Kiki).to(Openreach).with("message", "A Message"));
            interactions.capture(from(Openreach).to(Kiki).with("message", "A Message"));
        };
    }

    enum TestParty implements Party {
        Openreach,
        Kiki;

        @Override
        public String asString() {
            return this.name();
        }
    }
}
