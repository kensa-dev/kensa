package dev.kensa.example;

import dev.kensa.Kensa;
import dev.kensa.junit.KensaTest;
import dev.kensa.sentence.Acronym;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Path;

public abstract class JavaExampleTest implements KensaTest {
    @BeforeEach
    void setUpSourceLocations() {
        Kensa.configure()
                .withSourceLocations(Path.of(System.getProperty("user.dir"), "src/javaExample/java"))
                .withAcronyms(Acronym.of("FTTP", "Fibre To The Premises"))
                .withAcronyms(Acronym.of("FUBAR", "F***** up beyond all recognition"))
        ;
    }

}
