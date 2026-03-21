package dev.kensa.example;

import dev.kensa.Action;
import dev.kensa.GivensContext;
import dev.kensa.Kensa;
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

    Action<GivensContext> aMethodNameWithFttpAcronym() {
        return context -> {
        };
    }

    Action<GivensContext> aMethodNameWithFubarAcronym() {
        return context -> {
        };
    }
}
