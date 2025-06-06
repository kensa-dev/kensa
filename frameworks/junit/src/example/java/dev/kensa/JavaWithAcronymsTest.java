package dev.kensa;

import dev.kensa.junit.KensaTest;
import dev.kensa.sentence.Acronym;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JavaWithAcronymsTest implements KensaTest {

    @BeforeEach
    void setUp() {
        Kensa.configure()
                .withAcronyms(Acronym.of("FTTP", "Fibre To The Premises"))
                .withAcronyms(Acronym.of("FUBAR", "F***** up beyond all recognition"))
        ;
    }

    @Test
    void passingTest() {
        given(aMethodNameWithFttpAcronym());

        and(aMethodNameWithFubarAcronym());
    }

    GivensBuilderWithFixtures aMethodNameWithFttpAcronym() {
        return (givens, fixtures) -> {
        };
    }

    GivensBuilderWithFixtures aMethodNameWithFubarAcronym() {
        return (givens, fixtures) -> {
        };
    }

}
