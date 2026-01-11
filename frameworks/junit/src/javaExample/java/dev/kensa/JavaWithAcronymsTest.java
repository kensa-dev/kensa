package dev.kensa;

import dev.kensa.sentence.Acronym;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JavaWithAcronymsTest extends JavaExampleTest {

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

    GivensBuilder aMethodNameWithFttpAcronym() {
        return (givens) -> {
        };
    }

    GivensBuilder aMethodNameWithFubarAcronym() {
        return (givens) -> {
        };
    }

}
